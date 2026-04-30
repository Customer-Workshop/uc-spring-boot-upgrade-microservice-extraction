from datetime import date, datetime

from pydantic import BaseModel


class InstrumentOut(BaseModel):
    id: int
    symbol: str
    name: str
    exchange: str
    instrument_type: str
    sector: str | None = None
    last_price: float
    change: float
    change_pct: float
    open_price: float
    high_price: float
    low_price: float
    close_price: float
    volume: int

    model_config = {"from_attributes": True}


class OHLCVOut(BaseModel):
    date: date
    open: float
    high: float
    low: float
    close: float
    volume: int

    model_config = {"from_attributes": True}


class OrderCreate(BaseModel):
    symbol: str
    exchange: str = "NSE"
    transaction_type: str  # BUY / SELL
    order_type: str = "MARKET"  # MARKET / LIMIT / SL / SL-M
    product: str = "CNC"  # CNC / MIS / NRML
    quantity: int
    price: float = 0.0
    trigger_price: float = 0.0
    disclosed_qty: int = 0
    tag: str | None = None


class OrderOut(BaseModel):
    id: int
    symbol: str
    exchange: str
    transaction_type: str
    order_type: str
    product: str
    quantity: int
    price: float
    trigger_price: float
    disclosed_qty: int
    status: str
    filled_qty: int
    average_price: float
    order_timestamp: datetime | None = None
    tag: str | None = None

    model_config = {"from_attributes": True}


class HoldingOut(BaseModel):
    symbol: str
    exchange: str
    quantity: int
    average_price: float
    last_price: float
    pnl: float
    day_change: float
    day_change_pct: float

    model_config = {"from_attributes": True}


class PositionOut(BaseModel):
    symbol: str
    exchange: str
    product: str
    quantity: int
    buy_qty: int
    sell_qty: int
    buy_price: float
    sell_price: float
    last_price: float
    pnl: float

    model_config = {"from_attributes": True}


class WatchlistOut(BaseModel):
    id: int
    name: str
    items: list["WatchlistItemOut"] = []

    model_config = {"from_attributes": True}


class WatchlistItemOut(BaseModel):
    id: int
    symbol: str
    exchange: str
    last_price: float = 0.0
    change: float = 0.0
    change_pct: float = 0.0

    model_config = {"from_attributes": True}


class WatchlistAddItem(BaseModel):
    symbol: str
    exchange: str = "NSE"


class FundOut(BaseModel):
    equity_available: float
    equity_used: float
    commodity_available: float
    commodity_used: float
    opening_balance: float
    payin: float
    payout: float
    collateral: float

    model_config = {"from_attributes": True}


class MarketIndex(BaseModel):
    name: str
    value: float
    change: float
    change_pct: float


class DashboardOut(BaseModel):
    indices: list[MarketIndex]
    holdings_count: int
    holdings_value: float
    holdings_investment: float
    holdings_pnl: float
    holdings_day_pnl: float
    positions_count: int
    positions_pnl: float


class SearchResult(BaseModel):
    symbol: str
    name: str
    exchange: str
    instrument_type: str
    last_price: float

    model_config = {"from_attributes": True}
