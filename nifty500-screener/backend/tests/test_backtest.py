"""Unit tests for backtesting engine."""

from datetime import date, timedelta

import numpy as np
import pandas as pd
import pytest

from app.schemas.backtest import BacktestRequest
from app.services.backtest import run_backtest, apply_slippage, apply_commission


@pytest.fixture
def sample_data():
    np.random.seed(42)
    n = 300
    dates = [date(2023, 1, 2) + timedelta(days=i) for i in range(n)]
    close = 100 + np.cumsum(np.random.randn(n) * 2)
    close = np.maximum(close, 10)
    high = close + np.abs(np.random.randn(n))
    low = close - np.abs(np.random.randn(n))
    open_ = close + np.random.randn(n) * 0.5
    volume = np.random.randint(100000, 10000000, n).astype(float)

    df = pd.DataFrame({
        "date": dates,
        "open": open_,
        "high": high,
        "low": low,
        "close": close,
        "volume": volume,
    })
    df = df.set_index("date", drop=False)
    return {"TESTSTOCK": df}


def test_apply_slippage():
    buy_price = apply_slippage(100.0, 0.1, is_buy=True)
    sell_price = apply_slippage(100.0, 0.1, is_buy=False)
    assert buy_price > 100.0
    assert sell_price < 100.0


def test_apply_commission():
    comm = apply_commission(100000.0, 0.05)
    assert comm == pytest.approx(50.0)


def test_run_backtest_breakout(sample_data):
    request = BacktestRequest(
        strategy="breakout",
        initial_capital=1_000_000,
        symbols=["TESTSTOCK"],
    )
    result = run_backtest(sample_data, request)
    assert result.strategy == "breakout"
    assert result.initial_capital == 1_000_000
    assert result.metrics.total_trades >= 0
    assert len(result.equity_curve) > 0


def test_run_backtest_rsi_mean_reversion(sample_data):
    request = BacktestRequest(
        strategy="rsi_mean_reversion",
        initial_capital=1_000_000,
        symbols=["TESTSTOCK"],
    )
    result = run_backtest(sample_data, request)
    assert result.strategy == "rsi_mean_reversion"


def test_run_backtest_empty_data():
    request = BacktestRequest(strategy="breakout")
    result = run_backtest({}, request)
    assert result.metrics.total_trades == 0
    assert len(result.equity_curve) == 0
