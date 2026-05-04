import re
import uuid
from datetime import datetime, timezone
from typing import Optional

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func
from sqlalchemy.orm import Session, joinedload

from app.database import get_db
from app.models import Article, Tag, User, article_favorites, article_tags, follows
from app.schemas import (
    ArticleResponse,
    MultipleArticlesResponse,
    NewArticleWrapper,
    ProfileResponse,
    SingleArticleResponse,
    UpdateArticleWrapper,
)
from app.security import get_current_user, get_optional_user

router = APIRouter(prefix="/api/articles", tags=["Articles"])


def _format_datetime(dt: datetime) -> str:
    if dt.tzinfo is None:
        dt = dt.replace(tzinfo=timezone.utc)
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"


def _to_slug(title: str) -> str:
    return re.sub(r"[\s&|\ufe30-\uffa0'\"?,.]+", "-", title.lower())


def _build_article_response(
    article: Article,
    db: Session,
    current_user: Optional[User] = None,
) -> ArticleResponse:
    tag_names = sorted([t.name for t in article.tags])

    favorites_count = (
        db.query(func.count())
        .select_from(article_favorites)
        .filter(article_favorites.c.article_id == article.id)
        .scalar()
    ) or 0

    favorited = False
    author_following = False
    if current_user is not None:
        favorited = (
            db.query(func.count())
            .select_from(article_favorites)
            .filter(
                article_favorites.c.article_id == article.id,
                article_favorites.c.user_id == current_user.id,
            )
            .scalar()
            or 0
        ) > 0

        author_following = (
            db.query(func.count())
            .select_from(follows)
            .filter(
                follows.c.user_id == current_user.id,
                follows.c.follow_id == article.user_id,
            )
            .scalar()
            or 0
        ) > 0

    author = article.author
    return ArticleResponse(
        slug=article.slug,
        title=article.title,
        description=article.description,
        body=article.body,
        favorited=favorited,
        favoritesCount=favorites_count,
        createdAt=_format_datetime(article.created_at),
        updatedAt=_format_datetime(article.updated_at),
        tagList=tag_names,
        author=ProfileResponse(
            username=author.username,
            bio=author.bio,
            image=author.image,
            following=author_following,
        ),
    )


@router.get("/feed", response_model=MultipleArticlesResponse)
def get_feed(
    offset: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    followed_ids = (
        db.query(follows.c.follow_id)
        .filter(follows.c.user_id == current_user.id)
        .all()
    )
    followed_ids = [fid[0] for fid in followed_ids]

    if not followed_ids:
        return MultipleArticlesResponse(articles=[], articlesCount=0)

    query = (
        db.query(Article)
        .options(joinedload(Article.author), joinedload(Article.tags))
        .filter(Article.user_id.in_(followed_ids))
        .order_by(Article.created_at.desc())
    )

    total = (
        db.query(func.count(Article.id))
        .filter(Article.user_id.in_(followed_ids))
        .scalar()
    ) or 0

    articles = query.offset(offset).limit(limit).all()

    return MultipleArticlesResponse(
        articles=[_build_article_response(a, db, current_user) for a in articles],
        articlesCount=total,
    )


@router.get("", response_model=MultipleArticlesResponse)
def get_articles(
    offset: int = Query(0, ge=0),
    limit: int = Query(20, ge=1, le=100),
    tag: Optional[str] = Query(None),
    favorited: Optional[str] = Query(None),
    author: Optional[str] = Query(None),
    current_user: Optional[User] = Depends(get_optional_user),
    db: Session = Depends(get_db),
):
    query = db.query(Article).options(
        joinedload(Article.author), joinedload(Article.tags)
    )
    count_query = db.query(func.count(Article.id))

    if tag:
        query = query.join(article_tags).join(Tag).filter(Tag.name == tag)
        count_query = count_query.join(article_tags).join(Tag).filter(Tag.name == tag)

    if author:
        query = query.join(Article.author).filter(User.username == author)
        count_query = count_query.join(Article.author).filter(User.username == author)

    if favorited:
        fav_user = db.query(User).filter(User.username == favorited).first()
        if fav_user:
            query = query.join(article_favorites).filter(
                article_favorites.c.user_id == fav_user.id
            )
            count_query = count_query.join(article_favorites).filter(
                article_favorites.c.user_id == fav_user.id
            )
        else:
            return MultipleArticlesResponse(articles=[], articlesCount=0)

    total = count_query.scalar() or 0

    articles = (
        query.order_by(Article.created_at.desc())
        .offset(offset)
        .limit(limit)
        .all()
    )
    seen_ids: set[str] = set()
    unique_articles: list[Article] = []
    for a in articles:
        if a.id not in seen_ids:
            seen_ids.add(a.id)
            unique_articles.append(a)
    articles = unique_articles

    return MultipleArticlesResponse(
        articles=[_build_article_response(a, db, current_user) for a in articles],
        articlesCount=total,
    )


@router.get("/{slug}", response_model=SingleArticleResponse)
def get_article(
    slug: str,
    current_user: Optional[User] = Depends(get_optional_user),
    db: Session = Depends(get_db),
):
    article = (
        db.query(Article)
        .options(joinedload(Article.author), joinedload(Article.tags))
        .filter(Article.slug == slug)
        .first()
    )
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    return SingleArticleResponse(
        article=_build_article_response(article, db, current_user)
    )


@router.post("", response_model=SingleArticleResponse)
def create_article(
    payload: NewArticleWrapper,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    params = payload.article

    if not params.title or not params.title.strip():
        raise HTTPException(status_code=422, detail={"errors": {"title": ["can't be empty"]}})
    if not params.description or not params.description.strip():
        raise HTTPException(status_code=422, detail={"errors": {"description": ["can't be empty"]}})
    if not params.body or not params.body.strip():
        raise HTTPException(status_code=422, detail={"errors": {"body": ["can't be empty"]}})

    slug = _to_slug(params.title)

    existing = db.query(Article).filter(Article.slug == slug).first()
    if existing is not None:
        raise HTTPException(
            status_code=422,
            detail={"errors": {"title": ["duplicated article"]}},
        )

    now = datetime.now(timezone.utc)
    article = Article(
        id=str(uuid.uuid4()),
        user_id=current_user.id,
        slug=slug,
        title=params.title,
        description=params.description,
        body=params.body,
        created_at=now,
        updated_at=now,
    )

    for tag_name in set(params.tagList):
        tag = db.query(Tag).filter(Tag.name == tag_name).first()
        if tag is None:
            tag = Tag(id=str(uuid.uuid4()), name=tag_name)
            db.add(tag)
        article.tags.append(tag)

    db.add(article)
    db.commit()
    db.refresh(article)

    return SingleArticleResponse(
        article=_build_article_response(article, db, current_user)
    )


@router.put("/{slug}", response_model=SingleArticleResponse)
def update_article(
    slug: str,
    payload: UpdateArticleWrapper,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = (
        db.query(Article)
        .options(joinedload(Article.author), joinedload(Article.tags))
        .filter(Article.slug == slug)
        .first()
    )
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    if article.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    params = payload.article
    if params.title is not None and params.title.strip():
        article.title = params.title
        article.slug = _to_slug(params.title)
        article.updated_at = datetime.now(timezone.utc)
    if params.description is not None and params.description.strip():
        article.description = params.description
        article.updated_at = datetime.now(timezone.utc)
    if params.body is not None and params.body.strip():
        article.body = params.body
        article.updated_at = datetime.now(timezone.utc)

    db.commit()
    db.refresh(article)

    return SingleArticleResponse(
        article=_build_article_response(article, db, current_user)
    )


@router.delete("/{slug}", status_code=204)
def delete_article(
    slug: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    if article.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")

    db.delete(article)
    db.commit()
