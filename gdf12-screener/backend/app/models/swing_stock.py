from sqlalchemy import Boolean, Column, Float, Integer, String, Text

from app.database import Base


class SwingStock(Base):
    __tablename__ = "swing_stocks"

    id = Column(Integer, primary_key=True, autoincrement=True)
    symbol = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(300), nullable=False)
    sector = Column(String(100), nullable=False)
    exchange = Column(String(10), default="NSE")

    # Price
    current_price = Column(Float, default=0.0)
    prev_close = Column(Float, default=0.0)
    high_52w = Column(Float, default=0.0)
    low_52w = Column(Float, default=0.0)

    # Moving Averages
    sma_20 = Column(Float, default=0.0)
    sma_50 = Column(Float, default=0.0)
    sma_200 = Column(Float, default=0.0)
    ema_12 = Column(Float, default=0.0)
    ema_26 = Column(Float, default=0.0)

    # Momentum
    rsi_14 = Column(Float, default=0.0)
    macd_line = Column(Float, default=0.0)
    macd_signal = Column(Float, default=0.0)
    macd_histogram = Column(Float, default=0.0)

    # Volume
    volume = Column(Float, default=0.0)
    avg_volume_20 = Column(Float, default=0.0)
    volume_ratio = Column(Float, default=0.0)

    # Bollinger Bands
    bb_upper = Column(Float, default=0.0)
    bb_middle = Column(Float, default=0.0)
    bb_lower = Column(Float, default=0.0)
    bb_width = Column(Float, default=0.0)
    bb_squeeze = Column(Boolean, default=False)

    # ATR
    atr_14 = Column(Float, default=0.0)

    # Structure
    resistance_level = Column(Float, default=0.0)
    support_level = Column(Float, default=0.0)
    breakout_above_resistance = Column(Boolean, default=False)

    # Market cap for filtering
    market_cap_cr = Column(Float, default=0.0)


class SwingScore(Base):
    __tablename__ = "swing_scores"

    id = Column(Integer, primary_key=True, autoincrement=True)
    stock_id = Column(Integer, nullable=False, index=True)
    symbol = Column(String(50), nullable=False, index=True)

    # Confirmation Ladder (2 points each, max 10)
    trend_score = Column(Integer, default=0)
    trend_detail = Column(Text, nullable=True)

    momentum_score = Column(Integer, default=0)
    momentum_detail = Column(Text, nullable=True)

    volume_score = Column(Integer, default=0)
    volume_detail = Column(Text, nullable=True)

    volatility_score = Column(Integer, default=0)
    volatility_detail = Column(Text, nullable=True)

    structure_score = Column(Integer, default=0)
    structure_detail = Column(Text, nullable=True)

    total_score = Column(Integer, default=0)  # 0-10

    # Trade Setup
    signal = Column(String(20), default="")  # BUY, WATCH, SKIP
    entry_price = Column(Float, default=0.0)
    stop_loss = Column(Float, default=0.0)
    target_1 = Column(Float, default=0.0)
    target_2 = Column(Float, default=0.0)
    risk_reward = Column(Float, default=0.0)
    verdict = Column(String(50), default="")
