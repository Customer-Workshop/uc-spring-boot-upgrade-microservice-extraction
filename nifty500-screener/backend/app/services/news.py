"""News sentiment and macro impact service."""

from datetime import datetime

from sqlalchemy.orm import Session

from app.models.news import NewsItem


def get_stock_news(db: Session, stock_id: int, limit: int = 20) -> list[NewsItem]:
    return (
        db.query(NewsItem)
        .filter(NewsItem.stock_id == stock_id)
        .order_by(NewsItem.published_at.desc().nulls_last())
        .limit(limit)
        .all()
    )


def get_market_news(db: Session, limit: int = 50) -> list[NewsItem]:
    return (
        db.query(NewsItem)
        .filter(NewsItem.stock_id.is_(None))
        .order_by(NewsItem.published_at.desc().nulls_last())
        .limit(limit)
        .all()
    )


def compute_news_sentiment_score(news_items: list[NewsItem]) -> float:
    if not news_items:
        return 0.0
    scores = [n.sentiment_score for n in news_items if n.sentiment_score is not None]
    if not scores:
        return 0.0
    return sum(scores) / len(scores)
