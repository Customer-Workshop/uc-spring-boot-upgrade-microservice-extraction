from datetime import datetime

from sqlalchemy import Boolean, DateTime, Float, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class Stock(Base):
    __tablename__ = "stocks"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    symbol: Mapped[str] = mapped_column(String(20), unique=True, index=True, nullable=False)
    company_name: Mapped[str] = mapped_column(String(200), nullable=False)
    industry: Mapped[str] = mapped_column(String(100), nullable=True)
    sector: Mapped[str] = mapped_column(String(100), nullable=True)
    isin: Mapped[str] = mapped_column(String(20), nullable=True)
    market_cap: Mapped[float | None] = mapped_column(Float, nullable=True)
    is_nifty500: Mapped[bool] = mapped_column(Boolean, default=True)
    avg_daily_value: Mapped[float | None] = mapped_column(Float, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(
        DateTime, server_default=func.now(), onupdate=func.now()
    )

    ohlcv = relationship("OHLCV", back_populates="stock", cascade="all, delete-orphan")
    fundamentals = relationship("Fundamentals", back_populates="stock", cascade="all, delete-orphan")
    technical_signals = relationship(
        "TechnicalSignal", back_populates="stock", cascade="all, delete-orphan"
    )
    pattern_signals = relationship(
        "PatternSignal", back_populates="stock", cascade="all, delete-orphan"
    )
    scores = relationship("StockScore", back_populates="stock", cascade="all, delete-orphan")
    disclosures = relationship(
        "PromoterDisclosure", back_populates="stock", cascade="all, delete-orphan"
    )
    news_items = relationship("NewsItem", back_populates="stock", cascade="all, delete-orphan")

    def __repr__(self) -> str:
        return f"<Stock {self.symbol}>"
