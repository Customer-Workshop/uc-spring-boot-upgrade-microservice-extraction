from sqlalchemy import Column, Float, Integer

from app.database import Base


class FundAccount(Base):
    __tablename__ = "fund_accounts"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, unique=True, nullable=False, index=True)
    equity_available = Column(Float, default=500000.0)
    equity_used = Column(Float, default=0.0)
    commodity_available = Column(Float, default=0.0)
    commodity_used = Column(Float, default=0.0)
    opening_balance = Column(Float, default=500000.0)
    payin = Column(Float, default=0.0)
    payout = Column(Float, default=0.0)
    collateral = Column(Float, default=0.0)
