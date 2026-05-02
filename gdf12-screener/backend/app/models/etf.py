from sqlalchemy import Column, Float, Integer, String, Text

from app.database import Base


class ETF(Base):
    __tablename__ = "etfs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    symbol = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(300), nullable=False)
    category = Column(String(100), nullable=False)
    amc = Column(String(200), nullable=True)
    exchange = Column(String(10), default="NSE")

    # Price
    current_price = Column(Float, default=0.0)
    nav = Column(Float, default=0.0)
    high_52w = Column(Float, default=0.0)
    low_52w = Column(Float, default=0.0)

    # Moving Averages
    sma_20w = Column(Float, default=0.0)  # 20-week
    sma_50w = Column(Float, default=0.0)  # 50-week
    sma_10m = Column(Float, default=0.0)  # 10-month
    sma_12m = Column(Float, default=0.0)  # 12-month (200-day equiv)

    # Returns
    return_1w = Column(Float, default=0.0)
    return_1m = Column(Float, default=0.0)
    return_3m = Column(Float, default=0.0)
    return_6m = Column(Float, default=0.0)
    return_1y = Column(Float, default=0.0)
    return_3y_cagr = Column(Float, default=0.0)
    return_5y_cagr = Column(Float, default=0.0)

    # Costs & Liquidity
    expense_ratio = Column(Float, default=0.0)
    tracking_error = Column(Float, default=0.0)
    aum_cr = Column(Float, default=0.0)
    avg_volume = Column(Float, default=0.0)

    # Momentum
    rsi_14 = Column(Float, default=0.0)
    above_20w_ma = Column(Integer, default=0)  # 1=yes, 0=no
    above_50w_ma = Column(Integer, default=0)


class ETFScore(Base):
    __tablename__ = "etf_scores"

    id = Column(Integer, primary_key=True, autoincrement=True)
    etf_id = Column(Integer, nullable=False, index=True)
    symbol = Column(String(50), nullable=False, index=True)

    # Scoring factors (2 points each, max 10)
    trend_score = Column(Integer, default=0)
    trend_detail = Column(Text, nullable=True)

    momentum_score = Column(Integer, default=0)
    momentum_detail = Column(Text, nullable=True)

    cost_score = Column(Integer, default=0)
    cost_detail = Column(Text, nullable=True)

    liquidity_score = Column(Integer, default=0)
    liquidity_detail = Column(Text, nullable=True)

    performance_score = Column(Integer, default=0)
    performance_detail = Column(Text, nullable=True)

    total_score = Column(Integer, default=0)  # 0-10
    verdict = Column(String(50), default="")
