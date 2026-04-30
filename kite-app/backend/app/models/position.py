from sqlalchemy import Column, Float, Integer, String

from app.database import Base


class Position(Base):
    __tablename__ = "positions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, index=True)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(10), nullable=False, default="NSE")
    product = Column(String(10), nullable=False)  # CNC / MIS / NRML
    quantity = Column(Integer, nullable=False)
    buy_qty = Column(Integer, default=0)
    sell_qty = Column(Integer, default=0)
    buy_price = Column(Float, default=0.0)
    sell_price = Column(Float, default=0.0)
    last_price = Column(Float, default=0.0)
    pnl = Column(Float, default=0.0)
    day_buy_qty = Column(Integer, default=0)
    day_sell_qty = Column(Integer, default=0)
    day_buy_price = Column(Float, default=0.0)
    day_sell_price = Column(Float, default=0.0)
