from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.etf import ETF, ETFScore

router = APIRouter(prefix="/api/etf", tags=["etf"])


@router.get("/list")
def get_etfs(
    category: str | None = Query(None),
    min_score: int = Query(0, ge=0, le=10),
    sort_by: str = Query("score"),
    db: Session = Depends(get_db),
):
    query = db.query(ETF, ETFScore).outerjoin(ETFScore, ETF.id == ETFScore.etf_id)

    if category:
        query = query.filter(ETF.category == category)
    if min_score > 0:
        query = query.filter(ETFScore.total_score >= min_score)

    sort_map = {
        "score": ETFScore.total_score.desc(),
        "expense": ETF.expense_ratio.asc(),
        "return_1y": ETF.return_1y.desc(),
        "aum": ETF.aum_cr.desc(),
    }
    query = query.order_by(sort_map.get(sort_by, ETFScore.total_score.desc()))

    results = query.all()
    return [
        {
            "symbol": e.symbol, "name": e.name, "category": e.category,
            "amc": e.amc, "current_price": e.current_price, "nav": e.nav,
            "return_1m": e.return_1m, "return_3m": e.return_3m,
            "return_6m": e.return_6m, "return_1y": e.return_1y,
            "return_3y_cagr": e.return_3y_cagr, "return_5y_cagr": e.return_5y_cagr,
            "expense_ratio": e.expense_ratio, "tracking_error": e.tracking_error,
            "aum_cr": e.aum_cr, "rsi_14": e.rsi_14,
            "above_20w_ma": e.above_20w_ma == 1,
            "above_50w_ma": e.above_50w_ma == 1,
            "total_score": sc.total_score if sc else 0,
            "verdict": sc.verdict if sc else "Not Scored",
        }
        for e, sc in results
    ]


@router.get("/detail/{symbol}")
def get_etf_detail(symbol: str, db: Session = Depends(get_db)):
    etf = db.query(ETF).filter(ETF.symbol == symbol.upper()).first()
    if not etf:
        raise HTTPException(404, f"ETF {symbol} not found")

    score = db.query(ETFScore).filter(ETFScore.etf_id == etf.id).first()

    return {
        "symbol": etf.symbol, "name": etf.name, "category": etf.category,
        "amc": etf.amc, "current_price": etf.current_price, "nav": etf.nav,
        "high_52w": etf.high_52w, "low_52w": etf.low_52w,
        "sma_20w": etf.sma_20w, "sma_50w": etf.sma_50w,
        "sma_10m": etf.sma_10m, "sma_12m": etf.sma_12m,
        "return_1w": etf.return_1w, "return_1m": etf.return_1m,
        "return_3m": etf.return_3m, "return_6m": etf.return_6m,
        "return_1y": etf.return_1y, "return_3y_cagr": etf.return_3y_cagr,
        "return_5y_cagr": etf.return_5y_cagr,
        "expense_ratio": etf.expense_ratio, "tracking_error": etf.tracking_error,
        "aum_cr": etf.aum_cr, "avg_volume": etf.avg_volume,
        "rsi_14": etf.rsi_14, "above_20w_ma": etf.above_20w_ma == 1,
        "above_50w_ma": etf.above_50w_ma == 1,
        "score": {
            "total_score": score.total_score if score else 0,
            "verdict": score.verdict if score else "",
            "trend": {"score": score.trend_score, "detail": score.trend_detail} if score else None,
            "momentum": {"score": score.momentum_score, "detail": score.momentum_detail} if score else None,
            "cost": {"score": score.cost_score, "detail": score.cost_detail} if score else None,
            "liquidity": {"score": score.liquidity_score, "detail": score.liquidity_detail} if score else None,
            "performance": {"score": score.performance_score, "detail": score.performance_detail} if score else None,
        },
    }


@router.get("/categories")
def get_etf_categories(db: Session = Depends(get_db)):
    cats = db.query(ETF.category).distinct().all()
    return {"categories": sorted([c[0] for c in cats])}
