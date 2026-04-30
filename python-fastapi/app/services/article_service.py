import re
import uuid
from datetime import datetime, timezone
from typing import Optional

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.models import Article, Tag, User, article_favorites, article_tags, follows
from app.schemas.article import ArticleResponse, ProfileResponse


def to_slug(title: str) -> str:
    return re.sub(r"[&\u{fe30}-\u{ffa0}'\"\s?,\.]+", "-", title.lower())


def _slugify(title: str) -> str:
    slug = re.sub(r"[&'\"\s?,\.]+", "-", title.lower())
    slug = re.sub(r"-+", "-", slug).strip("-")
    return slug


def _build_profile(author: User, current_user: Optional[User], db: Session) -> ProfileResponse:
    is_following = False
    if current_user is not None:
        row = (
            db.query(follows)
            .filter(follows.c.user_id == current_user.id, follows.c.follow_id == author.id)
            .first()
        )
        is_following = row is not None
    return ProfileResponse(
        username=author.username,
        bio=author.bio,
        image=author.image,
        following=is_following,
    )


def _build_article_response(
    article: Article, current_user: Optional[User], db: Session
) -> ArticleResponse:
    tag_names = sorted([t.name for t in article.tags])
    fav_count = (
        db.query(func.count())
        .select_from(article_favorites)
        .filter(article_favorites.c.article_id == article.id)
        .scalar()
    )
    is_favorited = False
    if current_user is not None:
        fav_row = (
            db.query(article_favorites)
            .filter(
                article_favorites.c.article_id == article.id,
                article_favorites.c.user_id == current_user.id,
            )
            .first()
        )
        is_favorited = fav_row is not None

    profile = _build_profile(article.author, current_user, db)

    return ArticleResponse(
        id=article.id,
        slug=article.slug,
        title=article.title,
        description=article.description,
        body=article.body,
        favorited=is_favorited,
        favoritesCount=fav_count or 0,
        createdAt=article.created_at,
        updatedAt=article.updated_at,
        tagList=tag_names,
        author=profile,
    )


def get_article_by_slug(
    slug: str, current_user: Optional[User], db: Session
) -> Optional[ArticleResponse]:
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        return None
    return _build_article_response(article, current_user, db)


def list_articles(
    db: Session,
    current_user: Optional[User],
    tag: Optional[str] = None,
    author: Optional[str] = None,
    favorited: Optional[str] = None,
    offset: int = 0,
    limit: int = 20,
) -> tuple[list[ArticleResponse], int]:
    limit = min(limit, 100)
    query = db.query(Article)

    if tag:
        query = query.filter(
            Article.id.in_(
                db.query(article_tags.c.article_id)
                .join(Tag, Tag.id == article_tags.c.tag_id)
                .filter(Tag.name == tag)
            )
        )
    if author:
        query = query.filter(Article.user_id.in_(db.query(User.id).filter(User.username == author)))
    if favorited:
        query = query.filter(
            Article.id.in_(
                db.query(article_favorites.c.article_id).filter(
                    article_favorites.c.user_id.in_(
                        db.query(User.id).filter(User.username == favorited)
                    )
                )
            )
        )

    total = query.count()
    articles = query.order_by(Article.created_at.desc()).offset(offset).limit(limit).all()
    return [_build_article_response(a, current_user, db) for a in articles], total


def get_feed(
    db: Session, current_user: User, offset: int = 0, limit: int = 20
) -> tuple[list[ArticleResponse], int]:
    limit = min(limit, 100)
    followed_ids = (
        db.query(follows.c.follow_id).filter(follows.c.user_id == current_user.id).subquery()
    )
    query = db.query(Article).filter(Article.user_id.in_(db.query(followed_ids)))
    total = query.count()
    articles = query.order_by(Article.created_at.desc()).offset(offset).limit(limit).all()
    return [_build_article_response(a, current_user, db) for a in articles], total


def create_article(
    db: Session,
    current_user: User,
    title: str,
    description: str,
    body: str,
    tag_list: Optional[list[str]] = None,
) -> ArticleResponse:
    slug = _slugify(title)
    existing = db.query(Article).filter(Article.slug == slug).first()
    if existing:
        raise ValueError(f"Article with slug '{slug}' already exists")

    now = datetime.now(timezone.utc)
    article = Article(
        id=str(uuid.uuid4()),
        user_id=current_user.id,
        slug=slug,
        title=title,
        description=description,
        body=body,
        created_at=now,
        updated_at=now,
    )

    if tag_list:
        for tag_name in set(tag_list):
            existing_tag = db.query(Tag).filter(Tag.name == tag_name).first()
            if existing_tag:
                article.tags.append(existing_tag)
            else:
                new_tag = Tag(id=str(uuid.uuid4()), name=tag_name)
                db.add(new_tag)
                article.tags.append(new_tag)

    db.add(article)
    db.commit()
    db.refresh(article)
    return _build_article_response(article, current_user, db)


def update_article(
    db: Session,
    article: Article,
    current_user: User,
    title: Optional[str] = "",
    description: Optional[str] = "",
    body: Optional[str] = "",
) -> ArticleResponse:
    if title:
        article.title = title
        article.slug = _slugify(title)
        article.updated_at = datetime.now(timezone.utc)
    if description:
        article.description = description
        article.updated_at = datetime.now(timezone.utc)
    if body:
        article.body = body
        article.updated_at = datetime.now(timezone.utc)

    db.commit()
    db.refresh(article)
    return _build_article_response(article, current_user, db)


def delete_article(db: Session, article: Article) -> None:
    db.delete(article)
    db.commit()
