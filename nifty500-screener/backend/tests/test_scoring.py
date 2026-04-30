"""Unit tests for multi-factor scoring engine."""

import pytest

from app.services.scoring import (
    ScoringInput,
    ScoreResult,
    apply_hard_filters,
    compute_score,
    score_fundamentals,
    score_technical,
    score_patterns,
    score_valuation,
)


@pytest.fixture
def strong_stock():
    return ScoringInput(
        symbol="TEST",
        roe=25.0,
        roce=22.0,
        roa=12.0,
        gross_margin=55.0,
        ebitda_margin=30.0,
        net_margin=18.0,
        revenue_growth_3y=20.0,
        eps_growth_3y=25.0,
        debt_to_equity=0.5,
        interest_coverage=15.0,
        cfo_to_ni_ratio=1.2,
        fcf_yield=6.0,
        pe_zscore_5y=-0.5,
        pb_zscore_5y=-0.3,
        ev_ebitda_vs_sector=-0.2,
        peg_ratio=1.0,
        earnings_yield=8.0,
        rsi_14=55.0,
        distance_from_200sma=10.0,
        sma_200_slope=5.0,
        macd_histogram=2.0,
        rs_vs_nifty500=1.5,
        ma_crossover="golden_cross",
        breakout_detected=True,
        volume_expansion=True,
        higher_high=True,
        higher_low=True,
        trend_template_pass=True,
        promoter_buying=True,
        clustered_buys=True,
        news_sentiment_score=0.6,
        avg_daily_value=50_000_000,
        promoter_pledge_pct=5.0,
    )


@pytest.fixture
def weak_stock():
    return ScoringInput(
        symbol="WEAK",
        roe=3.0,
        roce=2.0,
        debt_to_equity=4.0,
        avg_daily_value=1_000_000,
        promoter_pledge_pct=60.0,
    )


def test_hard_filters_pass(strong_stock):
    failures = apply_hard_filters(strong_stock)
    assert len(failures) == 0


def test_hard_filters_fail(weak_stock):
    failures = apply_hard_filters(weak_stock)
    assert len(failures) >= 2


def test_score_fundamentals(strong_stock):
    score, details, missing = score_fundamentals(strong_stock)
    assert 0 <= score <= 100
    assert len(details) > 0
    assert "ROE" in details


def test_score_valuation(strong_stock):
    score, details, missing = score_valuation(strong_stock)
    assert 0 <= score <= 100


def test_score_technical(strong_stock):
    score, details, missing = score_technical(strong_stock)
    assert 0 <= score <= 100
    assert "RSI" in details


def test_score_patterns(strong_stock):
    score, details = score_patterns(strong_stock)
    assert score > 50  # strong stock should score well


def test_compute_score_strong(strong_stock):
    result = compute_score(strong_stock)
    assert isinstance(result, ScoreResult)
    assert result.passed_hard_filters is True
    assert result.total_score > 50
    assert result.reasons_text
    assert result.data_completeness > 0.5


def test_compute_score_weak(weak_stock):
    result = compute_score(weak_stock)
    assert result.passed_hard_filters is False
    assert len(result.filter_failures) >= 2


def test_missing_data_tracking():
    inp = ScoringInput(symbol="EMPTY")
    result = compute_score(inp)
    assert len(result.missing_data) > 0
    assert result.data_completeness < 1.0
