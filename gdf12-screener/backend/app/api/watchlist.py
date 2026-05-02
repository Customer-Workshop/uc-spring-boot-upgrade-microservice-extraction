from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.stock import Stock
from app.models.watchlist import WatchlistItem
from app.schemas.stock import WatchlistAddRequest, WatchlistItemResponse

router = APIRouter(prefix="/api", tags=["watchlist"])


@router.get("/watchlist", response_model=list[WatchlistItemResponse])
def get_watchlist(db: Session = Depends(get_db)):
    items = db.query(WatchlistItem).order_by(WatchlistItem.added_at.desc()).all()
    return [
        WatchlistItemResponse(
            id=item.id,
            symbol=item.symbol,
            added_at=item.added_at.isoformat() if item.added_at else "",
            notes=item.notes,
        )
        for item in items
    ]


@router.post("/watchlist", response_model=WatchlistItemResponse)
def add_to_watchlist(req: WatchlistAddRequest, db: Session = Depends(get_db)):
    symbol = req.symbol.upper()
    stock = db.query(Stock).filter(Stock.symbol == symbol).first()
    if not stock:
        raise HTTPException(status_code=404, detail=f"Stock {symbol} not found")

    existing = db.query(WatchlistItem).filter(WatchlistItem.symbol == symbol).first()
    if existing:
        raise HTTPException(status_code=409, detail=f"{symbol} already in watchlist")

    item = WatchlistItem(symbol=symbol, notes=req.notes)
    db.add(item)
    db.commit()
    db.refresh(item)
    return WatchlistItemResponse(
        id=item.id,
        symbol=item.symbol,
        added_at=item.added_at.isoformat() if item.added_at else "",
        notes=item.notes,
    )


@router.delete("/watchlist/{symbol}")
def remove_from_watchlist(symbol: str, db: Session = Depends(get_db)):
    item = db.query(WatchlistItem).filter(WatchlistItem.symbol == symbol.upper()).first()
    if not item:
        raise HTTPException(status_code=404, detail=f"{symbol} not in watchlist")
    db.delete(item)
    db.commit()
    return {"status": "removed", "symbol": symbol.upper()}
