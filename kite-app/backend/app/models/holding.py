from sqlalchemy import Column, Float, Integer, String

from app.database import Base


class Holding(Base):
    __tablename__ = "holdings"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, index=True)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(10), nullable=False, default="NSE")
    quantity = Column(Integer, nullable=False)
    average_price = Column(Float, nullable=False)
    last_price = Column(Float, default=0.0)
    pnl = Column(Float, default=0.0)
    day_change = Column(Float, default=0.0)
    day_change_pct = Column(Float, default=0.0)
