"""Query/command orchestration mirroring ArticleQueryService and the API layer."""

from datetime import datetime
from typing import List, Optional

from sqlalchemy.orm import Session

from . import repository as repo
from .models import User
from .schemas import ArticleOut, AuthorOut, CursorOut

MAX_LIMIT = 100


def normalize_page(offset: int, limit: int):
    """Mirror io.spring.application.Page normalization."""
    o = offset if offset > 0 else 0
    if limit > MAX_LIMIT:
        lim = MAX_LIMIT
    elif limit > 0:
        lim = limit
    else:
        lim = 20
    return o, lim


def _format_dt(dt: Optional[datetime]) -> Optional[str]:
    if dt is None:
        return None
    return dt.strftime("%Y-%m-%dT%H:%M:%S.") + f"{dt.microsecond // 1000:03d}Z"


def to_article_out(article: dict) -> ArticleOut:
    updated = _format_dt(article["updatedAt"])
    author = article["author"]
    return ArticleOut(
        id=article["id"],
        slug=article["slug"],
        title=article["title"],
        description=article["description"],
        body=article["body"],
        favorited=article["favorited"],
        favoritesCount=article["favoritesCount"],
        createdAt=_format_dt(article["createdAt"]),
        updatedAt=updated,
        tagList=article["tagList"],
        cursor=CursorOut(data=updated),
        author=AuthorOut(
            username=author["username"],
            bio=author["bio"],
            image=author["image"],
            following=author["following"],
        ),
    )


def _fill_single(db: Session, article: dict, user: Optional[User]) -> None:
    # Mirror ArticleQueryService#fillExtraInfo(id, user, articleData): only runs
    # when a user is present; otherwise favorited/favoritesCount/following stay
    # at their defaults (false / 0 / false).
    if user is None:
        return
    article["favorited"] = repo.is_user_favorite(db, user.id, article["id"])
    article["favoritesCount"] = repo.article_favorite_count(db, article["id"])
    article["author"]["following"] = repo.is_user_following(
        db, user.id, article["author"]["id"]
    )


def _fill_list(db: Session, articles: List[dict], user: Optional[User]) -> None:
    # Mirror ArticleQueryService#fillExtraInfo(list, currentUser).
    ids = [a["id"] for a in articles]
    counts = repo.articles_favorite_count(db, ids)
    for a in articles:
        a["favoritesCount"] = counts.get(a["id"], 0)
    if user is None:
        return
    favorited = repo.user_favorites(db, ids, user.id)
    author_ids = [a["author"]["id"] for a in articles]
    following = repo.following_authors(db, user.id, author_ids)
    for a in articles:
        if a["id"] in favorited:
            a["favorited"] = True
        if a["author"]["id"] in following:
            a["author"]["following"] = True


def find_by_slug(db: Session, slug: str, user: Optional[User]) -> Optional[dict]:
    article = repo.find_by_slug(db, slug)
    if article is None:
        return None
    _fill_single(db, article, user)
    return article


def find_by_id(db: Session, article_id: str, user: Optional[User]) -> Optional[dict]:
    article = repo.find_by_id(db, article_id)
    if article is None:
        return None
    _fill_single(db, article, user)
    return article


def find_recent_articles(
    db: Session, tag, author, favorited_by, offset: int, limit: int, user: Optional[User]
):
    o, lim = normalize_page(offset, limit)
    ids = repo.query_article_ids(db, tag, author, favorited_by, o, lim)
    count = repo.count_article(db, tag, author, favorited_by)
    if not ids:
        return [], count
    articles = repo.find_articles(db, ids)
    _fill_list(db, articles, user)
    return articles, count


def find_user_feed(db: Session, user: User, offset: int, limit: int):
    o, lim = normalize_page(offset, limit)
    followed = repo.followed_users(db, user.id)
    if not followed:
        return [], 0
    articles = repo.find_articles_of_authors(db, followed, o, lim)
    _fill_list(db, articles, user)
    count = repo.count_feed_size(db, followed)
    return articles, count
