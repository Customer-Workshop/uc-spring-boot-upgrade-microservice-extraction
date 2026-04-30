"""Scheduled Celery tasks for daily data refresh."""

import logging
from datetime import date

import numpy as np
import pandas as pd
from sqlalchemy.orm import Session

from app.database import SessionLocal
from app.models.ohlcv import OHLCV
from app.models.scores import StockScore
from app.models.signals import TechnicalSignal
from app.models.stock import Stock
from app.services.indicators import compute_all_technicals
from app.services.ingestion import seed_all_mock_data
from app.services.scoring import ScoringInput, compute_score
from app.tasks.celery_app import celery_app

logger = logging.getLogger(__name__)


@celery_app.task(name="app.tasks.scheduled.refresh_universe")
def refresh_universe() -> dict:
    db = SessionLocal()
    try:
        seed_all_mock_data(db)
        count = db.query(Stock).count()
        return {"status": "ok", "stocks": count}
    finally:
        db.close()


@celery_app.task(name="app.tasks.scheduled.recompute_signals")
def recompute_signals() -> dict:
    db = SessionLocal()
    try:
        stocks = db.query(Stock).filter(Stock.is_nifty500.is_(True)).all()
        computed = 0
        for stock in stocks:
            ohlcv_rows = (
                db.query(OHLCV)
                .filter(OHLCV.stock_id == stock.id, OHLCV.timeframe == "daily")
                .order_by(OHLCV.date)
                .all()
            )
            if len(ohlcv_rows) < 50:
                continue

            df = pd.DataFrame(
                [
                    {
                        "date": r.date,
                        "open": r.open,
                        "high": r.high,
                        "low": r.low,
                        "close": r.close,
                        "volume": r.volume,
                    }
                    for r in ohlcv_rows
                ]
            )
            tech_df = compute_all_technicals(df)
            last = tech_df.iloc[-1]

            signal = TechnicalSignal(
                stock_id=stock.id,
                date=date.today(),
                rsi_14=_safe_float(last.get("rsi_14")),
                sma_20=_safe_float(last.get("sma_20")),
                sma_50=_safe_float(last.get("sma_50")),
                sma_200=_safe_float(last.get("sma_200")),
                ema_20=_safe_float(last.get("ema_20")),
                ema_50=_safe_float(last.get("ema_50")),
                macd_line=_safe_float(last.get("macd_line")),
                macd_signal=_safe_float(last.get("macd_signal")),
                macd_histogram=_safe_float(last.get("macd_histogram")),
                bb_upper=_safe_float(last.get("bb_upper")),
                bb_middle=_safe_float(last.get("bb_middle")),
                bb_lower=_safe_float(last.get("bb_lower")),
                bb_width=_safe_float(last.get("bb_width")),
                atr_14=_safe_float(last.get("atr_14")),
                volatility_pct=_safe_float(last.get("volatility_pct")),
                ma_crossover=str(last.get("ma_crossover", "none")),
                distance_from_200sma=_safe_float(last.get("distance_from_200sma")),
                sma_200_slope=_safe_float(last.get("sma_200_slope")),
            )
            db.add(signal)
            computed += 1

        db.commit()
        return {"status": "ok", "computed": computed}
    finally:
        db.close()


@celery_app.task(name="app.tasks.scheduled.recompute_scores")
def recompute_scores() -> dict:
    db = SessionLocal()
    try:
        stocks = db.query(Stock).filter(Stock.is_nifty500.is_(True)).all()
        scores: list[StockScore] = []

        for stock in stocks:
            inp = _build_scoring_input(db, stock)
            result = compute_score(inp)

            score = StockScore(
                stock_id=stock.id,
                date=date.today(),
                total_score=result.total_score,
                fundamentals_score=result.fundamentals_score,
                valuation_score=result.valuation_score,
                technical_score=result.technical_score,
                pattern_score=result.pattern_score,
                insider_news_score=result.insider_news_score,
                passed_hard_filters=result.passed_hard_filters,
                filter_failures="; ".join(result.filter_failures),
                top_factors=result.top_factors,
                key_risks=result.key_risks,
                missing_data=result.missing_data,
                reasons_text=result.reasons_text,
                data_completeness=result.data_completeness,
            )
            scores.append(score)

        scores.sort(key=lambda s: s.total_score, reverse=True)
        for rank, score in enumerate(scores, 1):
            score.rank = rank
            db.add(score)

        db.commit()
        return {"status": "ok", "scored": len(scores)}
    finally:
        db.close()


def _build_scoring_input(db: Session, stock: Stock) -> ScoringInput:
    from app.models.fundamentals import Fundamentals
    from app.models.disclosures import PromoterDisclosure

    fund = (
        db.query(Fundamentals)
        .filter(Fundamentals.stock_id == stock.id)
        .order_by(Fundamentals.report_date.desc())
        .first()
    )

    signal = (
        db.query(TechnicalSignal)
        .filter(TechnicalSignal.stock_id == stock.id)
        .order_by(TechnicalSignal.date.desc())
        .first()
    )

    recent_disclosures = (
        db.query(PromoterDisclosure)
        .filter(PromoterDisclosure.stock_id == stock.id)
        .order_by(PromoterDisclosure.disclosure_date.desc())
        .limit(10)
        .all()
    )
    promoter_buying = any(d.transaction_type == "buy" for d in recent_disclosures)
    clustered = any(d.is_clustered_buy for d in recent_disclosures)
    pledge_pct = next(
        (d.promoter_pledge_pct for d in recent_disclosures if d.promoter_pledge_pct is not None),
        None,
    )

    return ScoringInput(
        symbol=stock.symbol,
        roe=fund.roe if fund else None,
        roce=fund.roce if fund else None,
        roa=fund.roa if fund else None,
        gross_margin=fund.gross_margin if fund else None,
        ebitda_margin=fund.ebitda_margin if fund else None,
        net_margin=fund.net_margin if fund else None,
        revenue_growth_3y=fund.revenue_growth_3y if fund else None,
        eps_growth_3y=fund.eps_growth_3y if fund else None,
        profit_cagr_3y=fund.profit_cagr_3y if fund else None,
        debt_to_equity=fund.debt_to_equity if fund else None,
        interest_coverage=fund.interest_coverage if fund else None,
        cfo_to_ni_ratio=fund.cfo_to_ni_ratio if fund else None,
        fcf_yield=fund.fcf_yield if fund else None,
        earnings_variability=fund.earnings_variability if fund else None,
        pe_ratio=fund.pe_ratio if fund else None,
        pb_ratio=fund.pb_ratio if fund else None,
        ev_to_ebitda=fund.ev_to_ebitda if fund else None,
        peg_ratio=fund.peg_ratio if fund else None,
        earnings_yield=fund.earnings_yield if fund else None,
        rsi_14=signal.rsi_14 if signal else None,
        distance_from_200sma=signal.distance_from_200sma if signal else None,
        sma_200_slope=signal.sma_200_slope if signal else None,
        macd_histogram=signal.macd_histogram if signal else None,
        rs_vs_nifty500=signal.rs_vs_nifty500 if signal else None,
        ma_crossover=signal.ma_crossover if signal else None,
        avg_daily_value=stock.avg_daily_value,
        promoter_buying=promoter_buying,
        promoter_pledge_pct=pledge_pct,
        clustered_buys=clustered,
    )


def _safe_float(val) -> float | None:
    if val is None or (isinstance(val, float) and (pd.isna(val) or np.isinf(val))):
        return None
    try:
        return float(val)
    except (TypeError, ValueError):
        return None
