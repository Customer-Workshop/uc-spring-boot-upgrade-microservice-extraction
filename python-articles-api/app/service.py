from __future__ import annotations

from typing import List, Optional

from sqlalchemy import func, literal_column, select
from sqlalchemy.orm import Session

from . import schemas
from .domain import format_datetime
from .models import Article, ArticleFavorite, Follow, Tag, User, article_tags


def _favorites_count(db: Session, article_id: str) -> int:
    return db.scalar(
        select(func.count())
        .select_from(ArticleFavorite)
        .where(ArticleFavorite.article_id == article_id)
    )


def _is_favorited(db: Session, article_id: str, user: Optional[User]) -> bool:
    if user is None:
        return False
    return (
        db.get(ArticleFavorite, {"article_id": article_id, "user_id": user.id})
        is not None
    )


def _is_following(db: Session, author_id: str, user: Optional[User]) -> bool:
    if user is None:
        return False
    return (
        db.scalar(
            select(func.count())
            .select_from(Follow)
            .where(Follow.user_id == user.id, Follow.follow_id == author_id)
        )
        > 0
    )


def _tag_list(db: Session, article_id: str) -> List[str]:
    # Preserve article_tags insertion order (mirrors the MyBatis left join).
    return list(
        db.scalars(
            select(Tag.name)
            .select_from(article_tags)
            .join(Tag, Tag.id == article_tags.c.tag_id)
            .where(article_tags.c.article_id == article_id)
            .order_by(literal_column("article_tags.rowid"))
        )
    )


def build_article_data(
    db: Session, article: Article, user: Optional[User], is_list: bool = False
) -> schemas.ArticleData:
    author: User = article.author
    updated_at = format_datetime(article.updated_at)
    # Single-article reads (findById/findBySlug) only fill favorite/follow info
    # when a user is present; the list/feed path always fills favoritesCount.
    favorites_count = (
        _favorites_count(db, article.id) if (is_list or user is not None) else 0
    )
    return schemas.ArticleData(
        id=article.id,
        slug=article.slug,
        title=article.title,
        description=article.description,
        body=article.body,
        favorited=_is_favorited(db, article.id, user),
        favoritesCount=favorites_count,
        createdAt=format_datetime(article.created_at),
        updatedAt=updated_at,
        tagList=_tag_list(db, article.id),
        cursor=schemas.Cursor(data=updated_at),
        author=schemas.ProfileData(
            username=author.username,
            bio=author.bio,
            image=author.image,
            following=_is_following(db, author.id, user),
        ),
    )


def find_by_slug(db: Session, slug: str) -> Optional[Article]:
    return db.scalar(select(Article).where(Article.slug == slug))


def find_by_id(db: Session, article_id: str) -> Optional[Article]:
    return db.get(Article, article_id)


def _recent_articles_query(
    tag: Optional[str], author: Optional[str], favorited_by: Optional[str]
):
    stmt = select(Article.id).select_from(Article)
    stmt = stmt.join(article_tags, article_tags.c.article_id == Article.id, isouter=True)
    stmt = stmt.join(Tag, Tag.id == article_tags.c.tag_id, isouter=True)
    stmt = stmt.join(
        ArticleFavorite, ArticleFavorite.article_id == Article.id, isouter=True
    )
    author_user = User.__table__.alias("AU")
    favoriter_user = User.__table__.alias("AFU")
    stmt = stmt.join(author_user, author_user.c.id == Article.user_id, isouter=True)
    stmt = stmt.join(
        favoriter_user, favoriter_user.c.id == ArticleFavorite.user_id, isouter=True
    )
    if tag is not None:
        stmt = stmt.where(Tag.name == tag)
    if author is not None:
        stmt = stmt.where(author_user.c.username == author)
    if favorited_by is not None:
        stmt = stmt.where(favoriter_user.c.username == favorited_by)
    return stmt


def find_recent_articles(
    db: Session,
    tag: Optional[str],
    author: Optional[str],
    favorited_by: Optional[str],
    offset: int,
    limit: int,
    user: Optional[User],
) -> schemas.MultipleArticlesResponse:
    subq = _recent_articles_query(tag, author, favorited_by).subquery()
    count = db.scalar(select(func.count(func.distinct(subq.c.id))))
    id_stmt = (
        _recent_articles_query(tag, author, favorited_by)
        .distinct()
        .order_by(Article.created_at.desc())
        .offset(offset)
        .limit(limit)
    )
    article_ids = list(db.scalars(id_stmt))
    if not article_ids:
        return schemas.MultipleArticlesResponse(articles=[], articlesCount=count)
    articles = list(
        db.scalars(
            select(Article)
            .where(Article.id.in_(article_ids))
            .order_by(Article.created_at.desc())
        )
    )
    return schemas.MultipleArticlesResponse(
        articles=[build_article_data(db, a, user, is_list=True) for a in articles],
        articlesCount=count,
    )


def find_user_feed(
    db: Session, user: User, offset: int, limit: int
) -> schemas.MultipleArticlesResponse:
    followed = list(
        db.scalars(select(Follow.follow_id).where(Follow.user_id == user.id))
    )
    if not followed:
        return schemas.MultipleArticlesResponse(articles=[], articlesCount=0)
    articles = list(
        db.scalars(
            select(Article)
            .where(Article.user_id.in_(followed))
            .order_by(literal_column("articles.rowid"))
            .offset(offset)
            .limit(limit)
        )
    )
    count = db.scalar(
        select(func.count()).select_from(Article).where(Article.user_id.in_(followed))
    )
    return schemas.MultipleArticlesResponse(
        articles=[build_article_data(db, a, user, is_list=True) for a in articles],
        articlesCount=count,
    )
