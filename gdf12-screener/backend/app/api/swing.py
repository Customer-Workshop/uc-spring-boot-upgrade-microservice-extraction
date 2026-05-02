from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.swing_stock import SwingScore, SwingStock

router = APIRouter(prefix="/api/swing", tags=["swing"])


@router.get("/stocks")
def get_swing_stocks(
    sector: str | None = Query(None),
    min_score: int = Query(0, ge=0, le=10),
    signal: str | None = Query(None),
    sort_by: str = Query("score"),
    db: Session = Depends(get_db),
):
    query = db.query(SwingStock, SwingScore).outerjoin(
        SwingScore, SwingStock.id == SwingScore.stock_id
    )
    if sector:
        query = query.filter(SwingStock.sector == sector)
    if min_score > 0:
        query = query.filter(SwingScore.total_score >= min_score)
    if signal:
        query = query.filter(SwingScore.signal == signal.upper())

    sort_map = {
        "score": SwingScore.total_score.desc(),
        "rsi": SwingStock.rsi_14.desc(),
        "volume": SwingStock.volume_ratio.desc(),
    }
    query = query.order_by(sort_map.get(sort_by, SwingScore.total_score.desc()))

    results = query.all()
    return [
        {
            "symbol": s.symbol, "name": s.name, "sector": s.sector,
            "current_price": s.current_price, "prev_close": s.prev_close,
            "change_pct": round((s.current_price - s.prev_close) / s.prev_close * 100, 2) if s.prev_close > 0 else 0,
            "rsi_14": s.rsi_14, "macd_histogram": s.macd_histogram,
            "volume_ratio": s.volume_ratio, "bb_squeeze": s.bb_squeeze,
            "breakout": s.breakout_above_resistance,
            "total_score": sc.total_score if sc else 0,
            "signal": sc.signal if sc else "N/A",
            "verdict": sc.verdict if sc else "",
            "entry_price": sc.entry_price if sc else 0,
            "stop_loss": sc.stop_loss if sc else 0,
            "target_1": sc.target_1 if sc else 0,
            "target_2": sc.target_2 if sc else 0,
            "risk_reward": sc.risk_reward if sc else 0,
        }
        for s, sc in results
    ]


@router.get("/stock/{symbol}")
def get_swing_detail(symbol: str, db: Session = Depends(get_db)):
    stock = db.query(SwingStock).filter(SwingStock.symbol == symbol.upper()).first()
    if not stock:
        raise HTTPException(404, f"Stock {symbol} not found")

    score = db.query(SwingScore).filter(SwingScore.stock_id == stock.id).first()

    return {
        "symbol": stock.symbol, "name": stock.name, "sector": stock.sector,
        "current_price": stock.current_price, "prev_close": stock.prev_close,
        "high_52w": stock.high_52w, "low_52w": stock.low_52w,
        "sma_20": stock.sma_20, "sma_50": stock.sma_50, "sma_200": stock.sma_200,
        "rsi_14": stock.rsi_14, "macd_line": stock.macd_line,
        "macd_signal": stock.macd_signal, "macd_histogram": stock.macd_histogram,
        "volume": stock.volume, "avg_volume_20": stock.avg_volume_20,
        "volume_ratio": stock.volume_ratio,
        "bb_upper": stock.bb_upper, "bb_middle": stock.bb_middle,
        "bb_lower": stock.bb_lower, "bb_width": stock.bb_width,
        "bb_squeeze": stock.bb_squeeze, "atr_14": stock.atr_14,
        "resistance_level": stock.resistance_level, "support_level": stock.support_level,
        "breakout_above_resistance": stock.breakout_above_resistance,
        "score": {
            "total_score": score.total_score if score else 0,
            "signal": score.signal if score else "",
            "verdict": score.verdict if score else "",
            "trend": {"score": score.trend_score, "detail": score.trend_detail} if score else None,
            "momentum": {"score": score.momentum_score, "detail": score.momentum_detail} if score else None,
            "volume": {"score": score.volume_score, "detail": score.volume_detail} if score else None,
            "volatility": {"score": score.volatility_score, "detail": score.volatility_detail} if score else None,
            "structure": {"score": score.structure_score, "detail": score.structure_detail} if score else None,
            "entry_price": score.entry_price if score else 0,
            "stop_loss": score.stop_loss if score else 0,
            "target_1": score.target_1 if score else 0,
            "target_2": score.target_2 if score else 0,
            "risk_reward": score.risk_reward if score else 0,
        },
    }
