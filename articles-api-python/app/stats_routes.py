"""Article statistics endpoints.

Endpoints:
  GET /api/articles/{slug}/stats  — view count, favorite count, comment count, days since published
  GET /api/stats/trending          — top 10 most-favorited articles in the last 7 days
"""

from datetime import datetime, timezone

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import func
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import Article, User, article_favorites

router = APIRouter(tags=["Statistics"])


def _get_current_user(db: Session) -> User | None:
    """Simplified auth stub: returns the first user in the DB or None."""
    return db.query(User).first()


@router.get("/api/articles/{slug}/stats", response_model=None)
def get_article_stats(slug: str, db: Session = Depends(get_db)):
    """GET /api/articles/{slug}/stats — returns article statistics."""
    article = db.query(Article).filter(Article.slug == slug).first()
    if article is None:
        raise HTTPException(status_code=404, detail="Article not found")

    # Favorite count
    favorite_count = (
        db.query(func.count())
        .select_from(article_favorites)
        .filter(article_favorites.c.article_id == article.id)
        .scalar()
    ) or 0

    # View count: not tracked in the data model, so we return 0
    # In a real app this would come from an analytics service or a views table
    view_count = 0

    # Comment count: comments table does not exist in the Python translation,
    # so we return 0. In the full Java app this would query the comments table.
    comment_count = 0

    # Days since published
    now = datetime.now(timezone.utc)
    created = article.created_at
    if created.tzinfo is None:
        created = created.replace(tzinfo=timezone.utc)
    days_since_published = (now - created).days

    return {
        "stats": {
            "slug": article.slug,
            "title": article.title,
            "viewCount": view_count,
            "favoriteCount": favorite_count,
            "commentCount": comment_count,
            "daysSincePublished": days_since_published,
        }
    }


@router.get("/api/stats/trending", response_model=None)
def get_trending_articles(db: Session = Depends(get_db)):
    """GET /api/stats/trending — top 10 most-favorited articles in the last 7 days."""
    current_user = _get_current_user(db)

    from datetime import timedelta

    seven_days_ago = datetime.now(timezone.utc) - timedelta(days=7)

    # Subquery: count favorites per article for articles created in the last 7 days
    results = (
        db.query(
            Article,
            func.count(article_favorites.c.user_id).label("fav_count"),
        )
        .outerjoin(article_favorites, Article.id == article_favorites.c.article_id)
        .filter(Article.created_at >= seven_days_ago)
        .group_by(Article.id)
        .order_by(func.count(article_favorites.c.user_id).desc())
        .limit(10)
        .all()
    )

    trending = []
    for article, fav_count in results:
        tag_names = sorted([t.name for t in article.tags])
        author = article.author

        now = datetime.now(timezone.utc)
        created = article.created_at
        if created.tzinfo is None:
            created = created.replace(tzinfo=timezone.utc)
        days_since_published = (now - created).days

        trending.append(
            {
                "slug": article.slug,
                "title": article.title,
                "description": article.description,
                "favoriteCount": fav_count,
                "daysSincePublished": days_since_published,
                "tagList": tag_names,
                "author": {
                    "username": author.username,
                    "bio": author.bio,
                    "image": author.image,
                },
            }
        )

    return {"articles": trending, "articlesCount": len(trending)}
