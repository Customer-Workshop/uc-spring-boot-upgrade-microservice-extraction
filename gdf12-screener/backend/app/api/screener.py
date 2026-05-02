from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.gdf_score import GDFScore
from app.models.stock import Stock
from app.schemas.stock import GDFScoreResponse, SignalDetail, StockDetail, StockSummary
from app.services.gdf_engine import compute_gdf_score, score_all_stocks

router = APIRouter(prefix="/api", tags=["screener"])


def _build_gdf_response(score: GDFScore) -> GDFScoreResponse:
    return GDFScoreResponse(
        symbol=score.symbol,
        total_score=score.total_score,
        graham_number=score.graham_number,
        margin_of_safety_pct=score.margin_of_safety_pct,
        verdict=score.verdict,
        group_a=[
            SignalDetail(passed=score.a1_large_stable, label="A1: Large & Stable Company", detail=score.a1_detail or ""),
            SignalDetail(passed=score.a2_consistent_profits, label="A2: Consistent Profits", detail=score.a2_detail or ""),
            SignalDetail(passed=score.a3_economic_moat, label="A3: Economic Moat", detail=score.a3_detail or ""),
        ],
        group_b=[
            SignalDetail(passed=score.b4_low_debt, label="B4: Low Debt", detail=score.b4_detail or ""),
            SignalDetail(passed=score.b5_healthy_returns, label="B5: Healthy Returns", detail=score.b5_detail or ""),
            SignalDetail(passed=score.b6_strong_cashflows, label="B6: Strong Cash Flows", detail=score.b6_detail or ""),
        ],
        group_c=[
            SignalDetail(passed=score.c7_reasonable_pe, label="C7: Reasonable PE", detail=score.c7_detail or ""),
            SignalDetail(passed=score.c8_graham_value, label="C8: Graham Value Check", detail=score.c8_detail or ""),
            SignalDetail(passed=score.c9_price_to_book, label="C9: Price to Book", detail=score.c9_detail or ""),
        ],
        group_d=[
            SignalDetail(passed=score.d10_promoter_quality, label="D10: Promoter Quality", detail=score.d10_detail or ""),
            SignalDetail(passed=score.d11_dividend_consistency, label="D11: Dividend Consistency", detail=score.d11_detail or ""),
            SignalDetail(passed=score.d12_silent_accumulation, label="D12: Silent Accumulation", detail=score.d12_detail or ""),
        ],
    )


@router.get("/screener", response_model=list[StockSummary])
def get_screener(
    sector: str | None = Query(None, description="Filter by sector"),
    min_score: int = Query(0, ge=0, le=12, description="Minimum GDF-12 score"),
    verdict: str | None = Query(None, description="Filter by verdict"),
    defensive_only: bool = Query(False, description="Only defensive sectors"),
    sort_by: str = Query("score", description="Sort by: score, pe, roe, mcap, mos"),
    limit: int = Query(50, ge=1, le=100),
    db: Session = Depends(get_db),
):
    query = db.query(Stock, GDFScore).outerjoin(GDFScore, Stock.id == GDFScore.stock_id)

    if sector:
        query = query.filter(Stock.sector == sector)
    if min_score > 0:
        query = query.filter(GDFScore.total_score >= min_score)
    if verdict:
        query = query.filter(GDFScore.verdict == verdict)
    if defensive_only:
        query = query.filter(Stock.is_defensive_sector.is_(True))

    sort_map = {
        "score": GDFScore.total_score.desc(),
        "pe": Stock.pe_ratio.asc(),
        "roe": Stock.roe.desc(),
        "mcap": Stock.market_cap_cr.desc(),
        "mos": GDFScore.margin_of_safety_pct.desc(),
    }
    order = sort_map.get(sort_by, GDFScore.total_score.desc())
    query = query.order_by(order)

    results = query.limit(limit).all()

    return [
        StockSummary(
            symbol=stock.symbol,
            name=stock.name,
            sector=stock.sector,
            current_price=stock.current_price,
            market_cap_cr=stock.market_cap_cr,
            pe_ratio=stock.pe_ratio,
            roe=stock.roe,
            roce=stock.roce,
            debt_to_equity=stock.debt_to_equity,
            promoter_holding_pct=stock.promoter_holding_pct,
            dividend_yield=stock.dividend_yield,
            is_defensive_sector=stock.is_defensive_sector,
            total_score=score.total_score if score else 0,
            verdict=score.verdict if score else "Not Scored",
            graham_number=score.graham_number if score else 0,
            margin_of_safety_pct=score.margin_of_safety_pct if score else 0,
        )
        for stock, score in results
    ]


@router.get("/stock/{symbol}", response_model=StockDetail)
def get_stock_detail(symbol: str, db: Session = Depends(get_db)):
    stock = db.query(Stock).filter(Stock.symbol == symbol.upper()).first()
    if not stock:
        raise HTTPException(status_code=404, detail=f"Stock {symbol} not found")

    score = db.query(GDFScore).filter(GDFScore.stock_id == stock.id).first()
    gdf = _build_gdf_response(score) if score else None

    return StockDetail(
        symbol=stock.symbol,
        name=stock.name,
        sector=stock.sector,
        industry=stock.industry,
        exchange=stock.exchange,
        current_price=stock.current_price,
        high_52w=stock.high_52w,
        low_52w=stock.low_52w,
        market_cap_cr=stock.market_cap_cr,
        sales_cr=stock.sales_cr,
        net_profit_cr=stock.net_profit_cr,
        pe_ratio=stock.pe_ratio,
        pe_5y_avg=stock.pe_5y_avg,
        pb_ratio=stock.pb_ratio,
        sector_avg_pb=stock.sector_avg_pb,
        eps=stock.eps,
        book_value=stock.book_value,
        roe=stock.roe,
        roce=stock.roce,
        debt_to_equity=stock.debt_to_equity,
        interest_coverage=stock.interest_coverage,
        ocf_cr=stock.ocf_cr,
        promoter_holding_pct=stock.promoter_holding_pct,
        promoter_pledge_pct=stock.promoter_pledge_pct,
        dividend_yield=stock.dividend_yield,
        dividend_years=stock.dividend_years,
        profit_years_positive=stock.profit_years_positive,
        ocf_positive_years=stock.ocf_positive_years,
        is_defensive_sector=stock.is_defensive_sector,
        moat_description=stock.moat_description,
        has_strong_brand=stock.has_strong_brand,
        has_cost_advantage=stock.has_cost_advantage,
        has_regulatory_barrier=stock.has_regulatory_barrier,
        has_dominant_share=stock.has_dominant_share,
        gdf_score=gdf,
    )


@router.get("/sectors")
def get_sectors(db: Session = Depends(get_db)):
    sectors = db.query(Stock.sector).distinct().all()
    return {"sectors": sorted([s[0] for s in sectors])}


@router.post("/score")
def compute_scores(db: Session = Depends(get_db)):
    count = score_all_stocks(db)
    return {"status": "scored", "stocks_scored": count}
