from datetime import date, datetime

from sqlalchemy import Date, DateTime, Float, ForeignKey, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class Fundamentals(Base):
    __tablename__ = "fundamentals"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    stock_id: Mapped[int] = mapped_column(Integer, ForeignKey("stocks.id"), nullable=False)
    report_date: Mapped[date] = mapped_column(Date, nullable=False)
    period: Mapped[str] = mapped_column(String(10), default="annual")  # annual, quarterly

    # Profitability
    roe: Mapped[float | None] = mapped_column(Float, nullable=True)
    roce: Mapped[float | None] = mapped_column(Float, nullable=True)
    roa: Mapped[float | None] = mapped_column(Float, nullable=True)
    gross_margin: Mapped[float | None] = mapped_column(Float, nullable=True)
    ebit_margin: Mapped[float | None] = mapped_column(Float, nullable=True)
    ebitda_margin: Mapped[float | None] = mapped_column(Float, nullable=True)
    net_margin: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Growth
    revenue: Mapped[float | None] = mapped_column(Float, nullable=True)
    net_income: Mapped[float | None] = mapped_column(Float, nullable=True)
    eps: Mapped[float | None] = mapped_column(Float, nullable=True)
    revenue_growth_3y: Mapped[float | None] = mapped_column(Float, nullable=True)
    revenue_growth_5y: Mapped[float | None] = mapped_column(Float, nullable=True)
    eps_growth_3y: Mapped[float | None] = mapped_column(Float, nullable=True)
    eps_growth_5y: Mapped[float | None] = mapped_column(Float, nullable=True)
    profit_cagr_3y: Mapped[float | None] = mapped_column(Float, nullable=True)
    profit_cagr_5y: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Balance sheet
    debt_to_equity: Mapped[float | None] = mapped_column(Float, nullable=True)
    net_debt_to_ebitda: Mapped[float | None] = mapped_column(Float, nullable=True)
    interest_coverage: Mapped[float | None] = mapped_column(Float, nullable=True)
    total_debt: Mapped[float | None] = mapped_column(Float, nullable=True)
    total_equity: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Cash flows
    operating_cashflow: Mapped[float | None] = mapped_column(Float, nullable=True)
    free_cashflow: Mapped[float | None] = mapped_column(Float, nullable=True)
    cfo_to_ni_ratio: Mapped[float | None] = mapped_column(Float, nullable=True)
    fcf_yield: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Efficiency
    asset_turnover: Mapped[float | None] = mapped_column(Float, nullable=True)
    working_capital_days: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Valuation
    pe_ratio: Mapped[float | None] = mapped_column(Float, nullable=True)
    pb_ratio: Mapped[float | None] = mapped_column(Float, nullable=True)
    ev_to_ebitda: Mapped[float | None] = mapped_column(Float, nullable=True)
    peg_ratio: Mapped[float | None] = mapped_column(Float, nullable=True)
    earnings_yield: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Moat proxies
    earnings_variability: Mapped[float | None] = mapped_column(Float, nullable=True)

    source: Mapped[str] = mapped_column(String(50), default="mock")
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    stock = relationship("Stock", back_populates="fundamentals")
