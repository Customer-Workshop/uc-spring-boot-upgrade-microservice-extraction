"""Universe API: manage NIFTY 500 stock list."""

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.stock import Stock
from app.schemas.stock import StockResponse
from app.services.ingestion import seed_universe

router = APIRouter(prefix="/universe", tags=["Universe"])


@router.get("", response_model=list[StockResponse])
def list_universe(
    sector: str | None = Query(None),
    search: str | None = Query(None),
    limit: int = Query(500, le=500),
    offset: int = Query(0, ge=0),
    db: Session = Depends(get_db),
):
    query = db.query(Stock).filter(Stock.is_nifty500.is_(True))
    if sector:
        query = query.filter(Stock.sector == sector)
    if search:
        pattern = f"%{search}%"
        query = query.filter(
            (Stock.symbol.ilike(pattern)) | (Stock.company_name.ilike(pattern))
        )
    return query.order_by(Stock.symbol).offset(offset).limit(limit).all()


@router.get("/sectors", response_model=list[str])
def list_sectors(db: Session = Depends(get_db)):
    rows = (
        db.query(Stock.sector)
        .filter(Stock.is_nifty500.is_(True), Stock.sector.isnot(None))
        .distinct()
        .order_by(Stock.sector)
        .all()
    )
    return [r[0] for r in rows]


@router.post("/seed", response_model=dict)
def seed_universe_data(db: Session = Depends(get_db)):
    stocks = seed_universe(db)
    return {"status": "ok", "count": len(stocks)}
