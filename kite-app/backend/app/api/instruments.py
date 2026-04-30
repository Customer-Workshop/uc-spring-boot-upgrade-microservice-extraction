"""Instrument search and market data endpoints."""

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.instrument import Instrument
from app.models.ohlcv import OHLCV
from app.schemas.schemas import InstrumentOut, MarketIndex, OHLCVOut, SearchResult

router = APIRouter(prefix="/api", tags=["instruments"])


@router.get("/instruments/search", response_model=list[SearchResult])
def search_instruments(
    q: str = Query(..., min_length=1),
    db: Session = Depends(get_db),
):
    results = (
        db.query(Instrument)
        .filter(
            (Instrument.symbol.ilike(f"%{q}%"))
            | (Instrument.name.ilike(f"%{q}%"))
        )
        .limit(20)
        .all()
    )
    return results


@router.get("/instruments/{symbol}", response_model=InstrumentOut)
def get_instrument(symbol: str, db: Session = Depends(get_db)):
    inst = db.query(Instrument).filter(Instrument.symbol == symbol).first()
    if not inst:
        from fastapi import HTTPException
        raise HTTPException(status_code=404, detail="Instrument not found")
    return inst


@router.get("/instruments/{symbol}/ohlcv", response_model=list[OHLCVOut])
def get_ohlcv(symbol: str, db: Session = Depends(get_db)):
    rows = (
        db.query(OHLCV)
        .filter(OHLCV.symbol == symbol)
        .order_by(OHLCV.date.asc())
        .all()
    )
    return rows


@router.get("/market/indices", response_model=list[MarketIndex])
def get_indices(db: Session = Depends(get_db)):
    index_symbols = ["NIFTY 50", "SENSEX", "BANKNIFTY"]
    indices = []
    for sym in index_symbols:
        inst = db.query(Instrument).filter(Instrument.symbol == sym).first()
        if inst:
            indices.append(
                MarketIndex(
                    name=sym,
                    value=inst.last_price,
                    change=inst.change,
                    change_pct=inst.change_pct,
                )
            )
    return indices
