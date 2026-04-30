"""Backtesting API."""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session
import pandas as pd

from app.database import get_db
from app.models.ohlcv import OHLCV
from app.models.stock import Stock
from app.schemas.backtest import BacktestRequest, BacktestResponse
from app.services.backtest import run_backtest

router = APIRouter(prefix="/backtest", tags=["Backtest"])


@router.post("/run", response_model=BacktestResponse)
def run_backtest_endpoint(request: BacktestRequest, db: Session = Depends(get_db)):
    if request.symbols:
        stocks = db.query(Stock).filter(Stock.symbol.in_(request.symbols)).all()
    else:
        stocks = db.query(Stock).filter(Stock.is_nifty500.is_(True)).limit(50).all()

    ohlcv_data: dict[str, pd.DataFrame] = {}
    for stock in stocks:
        rows = (
            db.query(OHLCV)
            .filter(OHLCV.stock_id == stock.id, OHLCV.timeframe == "daily")
            .order_by(OHLCV.date)
            .all()
        )
        if len(rows) < 50:
            continue

        df = pd.DataFrame(
            [
                {
                    "date": r.date,
                    "open": r.open,
                    "high": r.high,
                    "low": r.low,
                    "close": r.close,
                    "volume": r.volume,
                }
                for r in rows
            ]
        )
        df = df.set_index("date", drop=False)
        ohlcv_data[stock.symbol] = df

    result = run_backtest(ohlcv_data, request)
    return result
