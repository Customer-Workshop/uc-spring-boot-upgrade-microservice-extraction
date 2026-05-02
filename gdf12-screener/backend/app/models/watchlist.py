from datetime import datetime, timezone

from sqlalchemy import Column, DateTime, Integer, String

from app.database import Base


class WatchlistItem(Base):
    __tablename__ = "watchlist"

    id = Column(Integer, primary_key=True, autoincrement=True)
    symbol = Column(String(50), nullable=False, index=True, unique=True)
    added_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    notes = Column(String(500), nullable=True)
