from datetime import date, datetime

from sqlalchemy import JSON, Date, DateTime, Float, ForeignKey, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class StockScore(Base):
    __tablename__ = "stock_scores"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    stock_id: Mapped[int] = mapped_column(Integer, ForeignKey("stocks.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False, index=True)

    # Composite
    total_score: Mapped[float] = mapped_column(Float, default=0.0)
    rank: Mapped[int | None] = mapped_column(Integer, nullable=True)

    # Category scores (0-100)
    fundamentals_score: Mapped[float] = mapped_column(Float, default=0.0)
    valuation_score: Mapped[float] = mapped_column(Float, default=0.0)
    technical_score: Mapped[float] = mapped_column(Float, default=0.0)
    pattern_score: Mapped[float] = mapped_column(Float, default=0.0)
    insider_news_score: Mapped[float] = mapped_column(Float, default=0.0)

    # Gating
    passed_hard_filters: Mapped[bool] = mapped_column(default=False)
    filter_failures: Mapped[str | None] = mapped_column(String(500), nullable=True)

    # Explainability
    top_factors: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    key_risks: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    missing_data: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    reasons_text: Mapped[str | None] = mapped_column(String(2000), nullable=True)

    # Data completeness
    data_completeness: Mapped[float] = mapped_column(Float, default=0.0)

    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    stock = relationship("Stock", back_populates="scores")
