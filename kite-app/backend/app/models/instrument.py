from sqlalchemy import Column, Float, Integer, String

from app.database import Base


class Instrument(Base):
    __tablename__ = "instruments"

    id = Column(Integer, primary_key=True, autoincrement=True)
    symbol = Column(String(50), unique=True, nullable=False, index=True)
    name = Column(String(200), nullable=False)
    exchange = Column(String(10), nullable=False, default="NSE")
    instrument_type = Column(String(20), nullable=False, default="EQ")
    sector = Column(String(100), nullable=True)
    industry = Column(String(100), nullable=True)
    lot_size = Column(Integer, default=1)
    tick_size = Column(Float, default=0.05)
    last_price = Column(Float, default=0.0)
    change = Column(Float, default=0.0)
    change_pct = Column(Float, default=0.0)
    open_price = Column(Float, default=0.0)
    high_price = Column(Float, default=0.0)
    low_price = Column(Float, default=0.0)
    close_price = Column(Float, default=0.0)
    volume = Column(Integer, default=0)
