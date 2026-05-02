from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.etf import ETF, ETFScore
from app.models.gdf_score import GDFScore
from app.models.stock import Stock
from app.models.swing_stock import SwingScore, SwingStock
from app.services.ai_analysis import analyze_stock, get_ai_recommendation
from app.services.market_data import fetch_live_stock_data

router = APIRouter(prefix="/api/ai", tags=["ai"])


@router.get("/analyze/{symbol}")
async def ai_analyze(
    symbol: str,
    analysis_type: str = Query("fundamental"),
    db: Session = Depends(get_db),
):
    """AI-powered analysis of a stock/ETF."""
    data: dict = {"symbol": symbol.upper()}

    if analysis_type == "fundamental":
        stock = db.query(Stock).filter(Stock.symbol == symbol.upper()).first()
        if stock:
            score = db.query(GDFScore).filter(GDFScore.stock_id == stock.id).first()
            data.update({
                "name": stock.name, "sector": stock.sector,
                "current_price": stock.current_price, "pe_ratio": stock.pe_ratio,
                "pb_ratio": stock.pb_ratio, "eps": stock.eps,
                "book_value": stock.book_value, "roe": stock.roe, "roce": stock.roce,
                "debt_to_equity": stock.debt_to_equity,
                "interest_coverage": stock.interest_coverage,
                "market_cap_cr": stock.market_cap_cr, "sales_cr": stock.sales_cr,
                "promoter_holding_pct": stock.promoter_holding_pct,
                "promoter_pledge_pct": stock.promoter_pledge_pct,
                "dividend_yield": stock.dividend_yield,
                "dividend_years": stock.dividend_years,
                "profit_years_positive": stock.profit_years_positive,
                "ocf_positive_years": stock.ocf_positive_years,
                "is_defensive_sector": stock.is_defensive_sector,
                "moat_description": stock.moat_description,
            })
            if score:
                data.update({
                    "total_score": score.total_score,
                    "graham_number": score.graham_number,
                    "margin_of_safety_pct": score.margin_of_safety_pct,
                    "verdict": score.verdict,
                })

    elif analysis_type == "technical":
        swing = db.query(SwingStock).filter(SwingStock.symbol == symbol.upper()).first()
        if swing:
            sscore = db.query(SwingScore).filter(SwingScore.stock_id == swing.id).first()
            data.update({
                "name": swing.name, "sector": swing.sector,
                "current_price": swing.current_price,
                "sma_20": swing.sma_20, "sma_50": swing.sma_50, "sma_200": swing.sma_200,
                "rsi_14": swing.rsi_14, "macd_histogram": swing.macd_histogram,
                "volume_ratio": swing.volume_ratio,
                "bb_squeeze": swing.bb_squeeze,
                "breakout": swing.breakout_above_resistance,
                "atr_14": swing.atr_14,
                "resistance_level": swing.resistance_level,
                "support_level": swing.support_level,
            })
            if sscore:
                data.update({
                    "total_score": sscore.total_score, "signal": sscore.signal,
                    "verdict": sscore.verdict,
                    "entry_price": sscore.entry_price, "stop_loss": sscore.stop_loss,
                    "target_1": sscore.target_1, "target_2": sscore.target_2,
                    "risk_reward": sscore.risk_reward,
                })

    elif analysis_type == "etf":
        etf = db.query(ETF).filter(ETF.symbol == symbol.upper()).first()
        if etf:
            escore = db.query(ETFScore).filter(ETFScore.etf_id == etf.id).first()
            data.update({
                "name": etf.name, "category": etf.category,
                "current_price": etf.current_price,
                "return_1y": etf.return_1y, "return_3y_cagr": etf.return_3y_cagr,
                "return_5y_cagr": etf.return_5y_cagr,
                "expense_ratio": etf.expense_ratio,
                "tracking_error": etf.tracking_error,
                "aum_cr": etf.aum_cr,
                "above_20w_ma": etf.above_20w_ma == 1,
                "above_50w_ma": etf.above_50w_ma == 1,
            })
            if escore:
                data.update({
                    "total_score": escore.total_score,
                    "verdict": escore.verdict,
                })

    result = await analyze_stock(data, analysis_type)
    return result


@router.get("/recommend")
async def ai_recommend(
    tab: str = Query("fundamental"),
    db: Session = Depends(get_db),
):
    """Get AI top-3 recommendations for the selected tab."""
    stocks: list[dict] = []

    if tab == "fundamental":
        results = (
            db.query(Stock, GDFScore)
            .outerjoin(GDFScore, Stock.id == GDFScore.stock_id)
            .order_by(GDFScore.total_score.desc())
            .limit(15)
            .all()
        )
        stocks = [
            {
                "symbol": s.symbol, "name": s.name, "sector": s.sector,
                "current_price": s.current_price, "pe_ratio": s.pe_ratio,
                "roe": s.roe, "total_score": sc.total_score if sc else 0,
                "verdict": sc.verdict if sc else "",
                "margin_of_safety_pct": sc.margin_of_safety_pct if sc else 0,
            }
            for s, sc in results
        ]

    elif tab == "technical":
        results = (
            db.query(SwingStock, SwingScore)
            .outerjoin(SwingScore, SwingStock.id == SwingScore.stock_id)
            .order_by(SwingScore.total_score.desc())
            .limit(15)
            .all()
        )
        stocks = [
            {
                "symbol": s.symbol, "name": s.name, "sector": s.sector,
                "current_price": s.current_price, "rsi_14": s.rsi_14,
                "total_score": sc.total_score if sc else 0,
                "signal": sc.signal if sc else "",
            }
            for s, sc in results
        ]

    elif tab == "etf":
        results = (
            db.query(ETF, ETFScore)
            .outerjoin(ETFScore, ETF.id == ETFScore.etf_id)
            .order_by(ETFScore.total_score.desc())
            .limit(15)
            .all()
        )
        stocks = [
            {
                "symbol": e.symbol, "name": e.name,
                "current_price": e.current_price,
                "return_1y": e.return_1y, "expense_ratio": e.expense_ratio,
                "total_score": sc.total_score if sc else 0,
                "verdict": sc.verdict if sc else "",
            }
            for e, sc in results
        ]

    recommendation = await get_ai_recommendation(stocks, tab)
    return {
        "recommendation": recommendation,
        "model": "claude" if recommendation else "unavailable",
        "stocks_analyzed": len(stocks),
    }


@router.get("/live/{symbol}")
async def get_live_data(symbol: str):
    """Fetch real-time market data for a symbol."""
    data = fetch_live_stock_data(symbol.upper())
    if data:
        return {"status": "live", "data": data}
    return {"status": "mock", "data": None, "message": "Live data unavailable — using mock data"}
