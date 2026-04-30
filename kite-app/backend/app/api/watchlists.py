"""Watchlist management endpoints."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.instrument import Instrument
from app.models.watchlist import Watchlist, WatchlistItem
from app.schemas.schemas import WatchlistAddItem, WatchlistItemOut, WatchlistOut

router = APIRouter(prefix="/api", tags=["watchlists"])

DEMO_USER_ID = 1


@router.get("/watchlists", response_model=list[WatchlistOut])
def get_watchlists(db: Session = Depends(get_db)):
    wls = (
        db.query(Watchlist)
        .filter(Watchlist.user_id == DEMO_USER_ID)
        .order_by(Watchlist.position)
        .all()
    )
    result = []
    for wl in wls:
        items_db = (
            db.query(WatchlistItem)
            .filter(WatchlistItem.watchlist_id == wl.id)
            .order_by(WatchlistItem.position)
            .all()
        )
        items = []
        for wi in items_db:
            inst = db.query(Instrument).filter(Instrument.symbol == wi.symbol).first()
            items.append(
                WatchlistItemOut(
                    id=wi.id,
                    symbol=wi.symbol,
                    exchange=wi.exchange,
                    last_price=inst.last_price if inst else 0.0,
                    change=inst.change if inst else 0.0,
                    change_pct=inst.change_pct if inst else 0.0,
                )
            )
        result.append(WatchlistOut(id=wl.id, name=wl.name, items=items))
    return result


@router.post("/watchlists/{watchlist_id}/items", response_model=WatchlistItemOut)
def add_watchlist_item(
    watchlist_id: int,
    item: WatchlistAddItem,
    db: Session = Depends(get_db),
):
    wl = db.query(Watchlist).filter(Watchlist.id == watchlist_id).first()
    if not wl:
        raise HTTPException(status_code=404, detail="Watchlist not found")

    max_pos = (
        db.query(WatchlistItem)
        .filter(WatchlistItem.watchlist_id == watchlist_id)
        .count()
    )
    wi = WatchlistItem(
        watchlist_id=watchlist_id,
        symbol=item.symbol,
        exchange=item.exchange,
        position=max_pos,
    )
    db.add(wi)
    db.commit()
    db.refresh(wi)

    inst = db.query(Instrument).filter(Instrument.symbol == item.symbol).first()
    return WatchlistItemOut(
        id=wi.id,
        symbol=wi.symbol,
        exchange=wi.exchange,
        last_price=inst.last_price if inst else 0.0,
        change=inst.change if inst else 0.0,
        change_pct=inst.change_pct if inst else 0.0,
    )


@router.delete("/watchlists/{watchlist_id}/items/{item_id}")
def remove_watchlist_item(watchlist_id: int, item_id: int, db: Session = Depends(get_db)):
    wi = (
        db.query(WatchlistItem)
        .filter(WatchlistItem.id == item_id, WatchlistItem.watchlist_id == watchlist_id)
        .first()
    )
    if not wi:
        raise HTTPException(status_code=404, detail="Item not found")
    db.delete(wi)
    db.commit()
    return {"status": "deleted"}
