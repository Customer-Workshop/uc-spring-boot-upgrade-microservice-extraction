"""Unit tests for technical indicator calculations."""

import numpy as np
import pandas as pd
import pytest

from app.services.indicators import (
    compute_atr,
    compute_bollinger_bands,
    compute_ema,
    compute_macd,
    compute_rsi,
    compute_sma,
    detect_ma_crossover,
    compute_all_technicals,
)


@pytest.fixture
def sample_ohlcv():
    np.random.seed(42)
    n = 300
    dates = pd.date_range("2023-01-01", periods=n, freq="B")
    close = 100 + np.cumsum(np.random.randn(n) * 2)
    high = close + np.abs(np.random.randn(n))
    low = close - np.abs(np.random.randn(n))
    open_ = close + np.random.randn(n) * 0.5
    volume = np.random.randint(100000, 10000000, n).astype(float)

    return pd.DataFrame({
        "date": dates,
        "open": open_,
        "high": high,
        "low": low,
        "close": close,
        "volume": volume,
    })


def test_compute_rsi_range(sample_ohlcv):
    rsi = compute_rsi(sample_ohlcv["close"], 14)
    valid = rsi.dropna()
    assert len(valid) > 0
    assert valid.min() >= 0
    assert valid.max() <= 100


def test_compute_sma_length(sample_ohlcv):
    sma = compute_sma(sample_ohlcv["close"], 20)
    assert len(sma) == len(sample_ohlcv)
    assert sma.iloc[19:].notna().all()


def test_compute_ema_length(sample_ohlcv):
    ema = compute_ema(sample_ohlcv["close"], 20)
    assert len(ema) == len(sample_ohlcv)


def test_compute_macd_components(sample_ohlcv):
    macd_line, signal, histogram = compute_macd(sample_ohlcv["close"])
    assert len(macd_line) == len(sample_ohlcv)
    assert len(signal) == len(sample_ohlcv)
    assert len(histogram) == len(sample_ohlcv)


def test_compute_bollinger_bands(sample_ohlcv):
    upper, middle, lower, width = compute_bollinger_bands(sample_ohlcv["close"])
    valid_idx = upper.dropna().index
    assert (upper[valid_idx] >= middle[valid_idx]).all()
    assert (middle[valid_idx] >= lower[valid_idx]).all()
    assert (width[valid_idx] >= 0).all()


def test_compute_atr_positive(sample_ohlcv):
    atr = compute_atr(sample_ohlcv["high"], sample_ohlcv["low"], sample_ohlcv["close"])
    valid = atr.dropna()
    assert (valid >= 0).all()


def test_detect_ma_crossover(sample_ohlcv):
    sma_short = compute_sma(sample_ohlcv["close"], 20)
    sma_long = compute_sma(sample_ohlcv["close"], 50)
    result = detect_ma_crossover(sma_short, sma_long)
    assert set(result.unique()).issubset({"none", "golden_cross", "death_cross"})


def test_compute_all_technicals(sample_ohlcv):
    result = compute_all_technicals(sample_ohlcv)
    expected_cols = [
        "rsi_14", "sma_20", "sma_50", "sma_200", "ema_20", "ema_50",
        "macd_line", "macd_signal", "macd_histogram",
        "bb_upper", "bb_middle", "bb_lower", "bb_width",
        "atr_14", "volatility_pct", "ma_crossover",
        "distance_from_200sma", "sma_200_slope",
    ]
    for col in expected_cols:
        assert col in result.columns, f"Missing column: {col}"
