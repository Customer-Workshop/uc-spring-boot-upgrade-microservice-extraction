from datetime import datetime, timezone

from sqlalchemy import Boolean, Column, DateTime, Float, ForeignKey, Integer, String, Text

from app.database import Base


class GDFScore(Base):
    __tablename__ = "gdf_scores"

    id = Column(Integer, primary_key=True, autoincrement=True)
    stock_id = Column(Integer, ForeignKey("stocks.id"), nullable=False, index=True)
    symbol = Column(String(50), nullable=False, index=True)
    computed_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    # Group A: Business Quality
    a1_large_stable = Column(Boolean, default=False)
    a1_detail = Column(Text, nullable=True)
    a2_consistent_profits = Column(Boolean, default=False)
    a2_detail = Column(Text, nullable=True)
    a3_economic_moat = Column(Boolean, default=False)
    a3_detail = Column(Text, nullable=True)

    # Group B: Financial Strength
    b4_low_debt = Column(Boolean, default=False)
    b4_detail = Column(Text, nullable=True)
    b5_healthy_returns = Column(Boolean, default=False)
    b5_detail = Column(Text, nullable=True)
    b6_strong_cashflows = Column(Boolean, default=False)
    b6_detail = Column(Text, nullable=True)

    # Group C: Valuation
    c7_reasonable_pe = Column(Boolean, default=False)
    c7_detail = Column(Text, nullable=True)
    c8_graham_value = Column(Boolean, default=False)
    c8_detail = Column(Text, nullable=True)
    c9_price_to_book = Column(Boolean, default=False)
    c9_detail = Column(Text, nullable=True)

    # Group D: Shareholder & Market Signals
    d10_promoter_quality = Column(Boolean, default=False)
    d10_detail = Column(Text, nullable=True)
    d11_dividend_consistency = Column(Boolean, default=False)
    d11_detail = Column(Text, nullable=True)
    d12_silent_accumulation = Column(Boolean, default=False)
    d12_detail = Column(Text, nullable=True)

    # Aggregate
    total_score = Column(Integer, default=0)  # 0-12
    graham_number = Column(Float, default=0.0)
    margin_of_safety_pct = Column(Float, default=0.0)
    verdict = Column(String(50), default="")  # "Strong Buy", "Quality Candidate", "Watch", "Avoid"
