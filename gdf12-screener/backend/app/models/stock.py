from sqlalchemy import Boolean, Column, Float, Integer, String, Text

from app.database import Base


class Stock(Base):
    __tablename__ = "stocks"

    id = Column(Integer, primary_key=True, autoincrement=True)
    symbol = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(300), nullable=False)
    sector = Column(String(100), nullable=False)
    industry = Column(String(100), nullable=True)
    exchange = Column(String(10), default="NSE")

    # Price data
    current_price = Column(Float, default=0.0)
    high_52w = Column(Float, default=0.0)
    low_52w = Column(Float, default=0.0)

    # A1: Large & Stable
    market_cap_cr = Column(Float, default=0.0)
    sales_cr = Column(Float, default=0.0)

    # A2: Consistent Profits
    profit_years_positive = Column(Integer, default=0)  # out of 10
    net_profit_cr = Column(Float, default=0.0)

    # A3: Economic Moat (qualitative)
    has_strong_brand = Column(Boolean, default=False)
    has_cost_advantage = Column(Boolean, default=False)
    has_regulatory_barrier = Column(Boolean, default=False)
    has_dominant_share = Column(Boolean, default=False)
    moat_description = Column(Text, nullable=True)

    # B4: Low Debt
    debt_to_equity = Column(Float, default=0.0)
    interest_coverage = Column(Float, default=0.0)

    # B5: Healthy Returns
    roe = Column(Float, default=0.0)
    roce = Column(Float, default=0.0)

    # B6: Strong Cash Flows
    ocf_positive_years = Column(Integer, default=0)  # out of 10
    ocf_cr = Column(Float, default=0.0)
    ocf_tracks_profit = Column(Boolean, default=False)

    # C7: Reasonable PE
    pe_ratio = Column(Float, default=0.0)
    pe_5y_avg = Column(Float, default=0.0)

    # C8: Graham Value
    eps = Column(Float, default=0.0)
    book_value = Column(Float, default=0.0)

    # C9: Price to Book
    pb_ratio = Column(Float, default=0.0)
    sector_avg_pb = Column(Float, default=0.0)

    # D10: Promoter Quality
    promoter_holding_pct = Column(Float, default=0.0)
    promoter_pledge_pct = Column(Float, default=0.0)

    # D11: Dividend Consistency
    dividend_years = Column(Integer, default=0)  # out of 10
    dividend_yield = Column(Float, default=0.0)

    # D12: Silent Accumulation
    price_flat_months = Column(Integer, default=0)
    profit_growing = Column(Boolean, default=False)
    volume_rising = Column(Boolean, default=False)

    # Defensive sector flag
    is_defensive_sector = Column(Boolean, default=False)
