from sqlalchemy import Column, ForeignKey, Integer, String

from app.database import Base


class Watchlist(Base):
    __tablename__ = "watchlists"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, index=True)
    name = Column(String(100), nullable=False)
    position = Column(Integer, default=0)


class WatchlistItem(Base):
    __tablename__ = "watchlist_items"

    id = Column(Integer, primary_key=True, autoincrement=True)
    watchlist_id = Column(Integer, ForeignKey("watchlists.id"), nullable=False, index=True)
    symbol = Column(String(50), nullable=False)
    exchange = Column(String(10), default="NSE")
    position = Column(Integer, default=0)
