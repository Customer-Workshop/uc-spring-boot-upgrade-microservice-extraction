from datetime import date, datetime

from sqlalchemy import Date, DateTime, Float, ForeignKey, Integer, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.database import Base


class TechnicalSignal(Base):
    __tablename__ = "technical_signals"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    stock_id: Mapped[int] = mapped_column(Integer, ForeignKey("stocks.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False, index=True)

    # RSI
    rsi_14: Mapped[float | None] = mapped_column(Float, nullable=True)
    rsi_divergence: Mapped[str | None] = mapped_column(String(20), nullable=True)

    # Moving averages
    sma_20: Mapped[float | None] = mapped_column(Float, nullable=True)
    sma_50: Mapped[float | None] = mapped_column(Float, nullable=True)
    sma_200: Mapped[float | None] = mapped_column(Float, nullable=True)
    ema_20: Mapped[float | None] = mapped_column(Float, nullable=True)
    ema_50: Mapped[float | None] = mapped_column(Float, nullable=True)
    ma_crossover: Mapped[str | None] = mapped_column(String(30), nullable=True)
    distance_from_200sma: Mapped[float | None] = mapped_column(Float, nullable=True)
    sma_200_slope: Mapped[float | None] = mapped_column(Float, nullable=True)

    # MACD
    macd_line: Mapped[float | None] = mapped_column(Float, nullable=True)
    macd_signal: Mapped[float | None] = mapped_column(Float, nullable=True)
    macd_histogram: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Bollinger Bands
    bb_upper: Mapped[float | None] = mapped_column(Float, nullable=True)
    bb_middle: Mapped[float | None] = mapped_column(Float, nullable=True)
    bb_lower: Mapped[float | None] = mapped_column(Float, nullable=True)
    bb_width: Mapped[float | None] = mapped_column(Float, nullable=True)
    bb_squeeze: Mapped[bool | None] = mapped_column(nullable=True)

    # Volatility
    atr_14: Mapped[float | None] = mapped_column(Float, nullable=True)
    volatility_pct: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Relative strength
    rs_vs_nifty500: Mapped[float | None] = mapped_column(Float, nullable=True)
    rs_vs_nifty50: Mapped[float | None] = mapped_column(Float, nullable=True)

    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    stock = relationship("Stock", back_populates="technical_signals")


class PatternSignal(Base):
    __tablename__ = "pattern_signals"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    stock_id: Mapped[int] = mapped_column(Integer, ForeignKey("stocks.id"), nullable=False)
    date: Mapped[date] = mapped_column(Date, nullable=False, index=True)

    pattern_type: Mapped[str] = mapped_column(String(50), nullable=False)
    confidence: Mapped[float] = mapped_column(Float, default=0.0)
    description: Mapped[str | None] = mapped_column(String(500), nullable=True)

    # Breakout detection
    breakout_detected: Mapped[bool | None] = mapped_column(nullable=True)
    volume_expansion: Mapped[bool | None] = mapped_column(nullable=True)

    # Trend structure
    higher_high: Mapped[bool | None] = mapped_column(nullable=True)
    higher_low: Mapped[bool | None] = mapped_column(nullable=True)

    # Support/resistance
    nearest_support: Mapped[float | None] = mapped_column(Float, nullable=True)
    nearest_resistance: Mapped[float | None] = mapped_column(Float, nullable=True)
    sr_proximity: Mapped[str | None] = mapped_column(String(20), nullable=True)

    # Gap detection
    gap_up: Mapped[bool | None] = mapped_column(nullable=True)
    gap_pct: Mapped[float | None] = mapped_column(Float, nullable=True)

    # Weekly structure
    above_30w_ma: Mapped[bool | None] = mapped_column(nullable=True)
    trend_template_pass: Mapped[bool | None] = mapped_column(nullable=True)

    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    stock = relationship("Stock", back_populates="pattern_signals")
