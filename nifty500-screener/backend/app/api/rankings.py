"""Rankings API: daily top-N stock rankings."""

from datetime import date

from fastapi import APIRouter, Depends, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.scores import StockScore
from app.models.stock import Stock
from app.schemas.ranking import RankedStock, RankingsResponse

router = APIRouter(prefix="/rankings", tags=["Rankings"])


@router.get("/today", response_model=RankingsResponse)
def get_today_rankings(
    top_n: int = Query(20, le=100),
    sector: str | None = Query(None),
    db: Session = Depends(get_db),
):
    today = date.today()
    query = (
        db.query(StockScore, Stock)
        .join(Stock, StockScore.stock_id == Stock.id)
        .filter(StockScore.date == today, StockScore.passed_hard_filters.is_(True))
    )
    if sector:
        query = query.filter(Stock.sector == sector)

    results = query.order_by(StockScore.total_score.desc()).limit(top_n).all()

    total_universe = db.query(Stock).filter(Stock.is_nifty500.is_(True)).count()
    passed = (
        db.query(StockScore)
        .filter(StockScore.date == today, StockScore.passed_hard_filters.is_(True))
        .count()
    )

    rankings = []
    for score, stock in results:
        rankings.append(
            RankedStock(
                rank=score.rank or 0,
                symbol=stock.symbol,
                company_name=stock.company_name,
                sector=stock.sector,
                total_score=score.total_score,
                fundamentals_score=score.fundamentals_score,
                valuation_score=score.valuation_score,
                technical_score=score.technical_score,
                pattern_score=score.pattern_score,
                insider_news_score=score.insider_news_score,
                reasons_text=score.reasons_text,
                top_factors=score.top_factors,
                key_risks=score.key_risks,
            )
        )

    return RankingsResponse(
        date=today,
        total_universe=total_universe,
        passed_filters=passed,
        rankings=rankings,
    )


@router.get("/history", response_model=list[RankingsResponse])
def get_rankings_history(
    days: int = Query(7, le=30),
    top_n: int = Query(20, le=100),
    db: Session = Depends(get_db),
):
    from datetime import timedelta

    responses = []
    for i in range(days):
        d = date.today() - timedelta(days=i)
        results = (
            db.query(StockScore, Stock)
            .join(Stock, StockScore.stock_id == Stock.id)
            .filter(StockScore.date == d, StockScore.passed_hard_filters.is_(True))
            .order_by(StockScore.total_score.desc())
            .limit(top_n)
            .all()
        )
        if not results:
            continue

        rankings = [
            RankedStock(
                rank=score.rank or 0,
                symbol=stock.symbol,
                company_name=stock.company_name,
                sector=stock.sector,
                total_score=score.total_score,
                fundamentals_score=score.fundamentals_score,
                valuation_score=score.valuation_score,
                technical_score=score.technical_score,
                pattern_score=score.pattern_score,
                insider_news_score=score.insider_news_score,
                reasons_text=score.reasons_text,
                top_factors=score.top_factors,
                key_risks=score.key_risks,
            )
            for score, stock in results
        ]
        total = db.query(Stock).filter(Stock.is_nifty500.is_(True)).count()
        passed = (
            db.query(StockScore)
            .filter(StockScore.date == d, StockScore.passed_hard_filters.is_(True))
            .count()
        )
        responses.append(
            RankingsResponse(date=d, total_universe=total, passed_filters=passed, rankings=rankings)
        )

    return responses
