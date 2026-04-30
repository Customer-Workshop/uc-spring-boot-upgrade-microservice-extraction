from sqlalchemy import Column, DateTime, Float, Integer, String, func

from app.database import Base


class Order(Base):
    __tablename__ = "orders"

    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, nullable=False, index=True)
    symbol = Column(String(50), nullable=False, index=True)
    exchange = Column(String(10), nullable=False, default="NSE")
    transaction_type = Column(String(4), nullable=False)  # BUY / SELL
    order_type = Column(String(10), nullable=False)  # MARKET / LIMIT / SL / SL-M
    product = Column(String(10), nullable=False)  # CNC / MIS / NRML
    quantity = Column(Integer, nullable=False)
    price = Column(Float, default=0.0)
    trigger_price = Column(Float, default=0.0)
    disclosed_qty = Column(Integer, default=0)
    status = Column(String(20), nullable=False, default="OPEN")
    filled_qty = Column(Integer, default=0)
    average_price = Column(Float, default=0.0)
    order_timestamp = Column(DateTime, server_default=func.now())
    exchange_timestamp = Column(DateTime, nullable=True)
    tag = Column(String(50), nullable=True)
