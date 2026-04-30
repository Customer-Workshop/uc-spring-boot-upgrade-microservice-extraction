"""Stock detail API: snapshot, chart data, fundamentals, signals."""

from datetime import date, timedelta

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.disclosures import PromoterDisclosure
from app.models.fundamentals import Fundamentals
from app.models.news import NewsItem
from app.models.ohlcv import OHLCV
from app.models.scores import StockScore
from app.models.signals import TechnicalSignal
from app.models.stock import Stock
from app.schemas.stock import (
    DisclosureResponse,
    FundamentalsResponse,
    NewsItemResponse,
    OHLCVResponse,
    ScoreResponse,
    StockResponse,
    StockSnapshotResponse,
    TechnicalSignalResponse,
)

router = APIRouter(prefix="/stock", tags=["Stock"])


def _get_stock_or_404(db: Session, symbol: str) -> Stock:
    stock = db.query(Stock).filter(Stock.symbol == symbol.upper()).first()
    if not stock:
        raise HTTPException(status_code=404, detail=f"Stock {symbol} not found")
    return stock


@router.get("/{symbol}/snapshot", response_model=StockSnapshotResponse)
def get_snapshot(symbol: str, db: Session = Depends(get_db)):
    stock = _get_stock_or_404(db, symbol)

    latest_ohlcv = (
        db.query(OHLCV)
        .filter(OHLCV.stock_id == stock.id)
        .order_by(OHLCV.date.desc())
        .first()
    )
    fund = (
        db.query(Fundamentals)
        .filter(Fundamentals.stock_id == stock.id)
        .order_by(Fundamentals.report_date.desc())
        .first()
    )
    tech = (
        db.query(TechnicalSignal)
        .filter(TechnicalSignal.stock_id == stock.id)
        .order_by(TechnicalSignal.date.desc())
        .first()
    )
    disclosures = (
        db.query(PromoterDisclosure)
        .filter(PromoterDisclosure.stock_id == stock.id)
        .order_by(PromoterDisclosure.disclosure_date.desc())
        .limit(10)
        .all()
    )
    news = (
        db.query(NewsItem)
        .filter(NewsItem.stock_id == stock.id)
        .order_by(NewsItem.published_at.desc().nulls_last())
        .limit(5)
        .all()
    )
    score = (
        db.query(StockScore)
        .filter(StockScore.stock_id == stock.id)
        .order_by(StockScore.date.desc())
        .first()
    )

    return StockSnapshotResponse(
        stock=StockResponse.model_validate(stock),
        latest_price=OHLCVResponse.model_validate(latest_ohlcv) if latest_ohlcv else None,
        fundamentals=FundamentalsResponse.model_validate(fund) if fund else None,
        technical=TechnicalSignalResponse.model_validate(tech) if tech else None,
        disclosures=[DisclosureResponse.model_validate(d) for d in disclosures],
        news=[NewsItemResponse.model_validate(n) for n in news],
        score=ScoreResponse.model_validate(score) if score else None,
    )


@router.get("/{symbol}/chart", response_model=list[OHLCVResponse])
def get_chart_data(
    symbol: str,
    timeframe: str = Query("daily"),
    days: int = Query(365, le=2000),
    db: Session = Depends(get_db),
):
    stock = _get_stock_or_404(db, symbol)
    since = date.today() - timedelta(days=days)
    rows = (
        db.query(OHLCV)
        .filter(OHLCV.stock_id == stock.id, OHLCV.timeframe == timeframe, OHLCV.date >= since)
        .order_by(OHLCV.date)
        .all()
    )
    return [OHLCVResponse.model_validate(r) for r in rows]


@router.get("/{symbol}/fundamentals", response_model=list[FundamentalsResponse])
def get_fundamentals(
    symbol: str,
    period: str = Query("annual"),
    limit: int = Query(5),
    db: Session = Depends(get_db),
):
    stock = _get_stock_or_404(db, symbol)
    rows = (
        db.query(Fundamentals)
        .filter(Fundamentals.stock_id == stock.id, Fundamentals.period == period)
        .order_by(Fundamentals.report_date.desc())
        .limit(limit)
        .all()
    )
    return [FundamentalsResponse.model_validate(r) for r in rows]


@router.get("/{symbol}/technicals", response_model=list[TechnicalSignalResponse])
def get_technicals(
    symbol: str,
    days: int = Query(30, le=365),
    db: Session = Depends(get_db),
):
    stock = _get_stock_or_404(db, symbol)
    since = date.today() - timedelta(days=days)
    rows = (
        db.query(TechnicalSignal)
        .filter(TechnicalSignal.stock_id == stock.id, TechnicalSignal.date >= since)
        .order_by(TechnicalSignal.date.desc())
        .all()
    )
    return [TechnicalSignalResponse.model_validate(r) for r in rows]
