"""Price action pattern detection engine."""

import numpy as np
import pandas as pd


def detect_consolidation_breakout(
    df: pd.DataFrame, lookback: int = 20, vol_mult: float = 1.5
) -> pd.DataFrame:
    """Detect breakout from consolidation using range contraction + volume expansion."""
    result = pd.DataFrame(index=df.index)

    rolling_high = df["high"].rolling(window=lookback).max()
    rolling_low = df["low"].rolling(window=lookback).min()
    price_range = (rolling_high - rolling_low) / rolling_low * 100

    range_contracting = price_range < price_range.rolling(window=lookback).mean()
    avg_vol = df["volume"].rolling(window=lookback).mean()
    vol_expanding = df["volume"] > avg_vol * vol_mult
    breakout_up = df["close"] > rolling_high.shift(1)

    result["breakout_detected"] = range_contracting.shift(1) & breakout_up & vol_expanding
    result["volume_expansion"] = vol_expanding

    return result


def detect_trend_structure(df: pd.DataFrame, swing_window: int = 5) -> pd.DataFrame:
    """Detect higher-high / higher-low sequences."""
    result = pd.DataFrame(index=df.index)

    swing_highs = df["high"].rolling(window=swing_window, center=True).max() == df["high"]
    swing_lows = df["low"].rolling(window=swing_window, center=True).min() == df["low"]

    prev_swing_high = df["high"].where(swing_highs).ffill()
    prev_swing_low = df["low"].where(swing_lows).ffill()

    result["higher_high"] = (df["high"] > prev_swing_high.shift(1)) & swing_highs
    result["higher_low"] = (df["low"] > prev_swing_low.shift(1)) & swing_lows

    return result


def compute_support_resistance(
    df: pd.DataFrame, window: int = 20, num_levels: int = 5
) -> tuple[list[float], list[float]]:
    """Find approximate support/resistance levels using price pivots."""
    if len(df) < window:
        return [], []

    highs = df["high"].values
    lows = df["low"].values

    pivot_highs: list[float] = []
    pivot_lows: list[float] = []

    half = window // 2
    for i in range(half, len(highs) - half):
        if highs[i] == max(highs[i - half : i + half + 1]):
            pivot_highs.append(float(highs[i]))
        if lows[i] == min(lows[i - half : i + half + 1]):
            pivot_lows.append(float(lows[i]))

    resistance = sorted(set(pivot_highs), reverse=True)[:num_levels]
    support = sorted(set(pivot_lows))[:num_levels]
    return support, resistance


def detect_gap(df: pd.DataFrame, min_gap_pct: float = 1.0) -> pd.DataFrame:
    """Detect gap-ups and gap-downs."""
    result = pd.DataFrame(index=df.index)
    prev_high = df["high"].shift(1)
    prev_low = df["low"].shift(1)

    gap_up_pct = (df["low"] - prev_high) / prev_high * 100
    result["gap_up"] = gap_up_pct > min_gap_pct
    result["gap_pct"] = gap_up_pct.where(gap_up_pct.abs() >= min_gap_pct, 0.0)

    return result


def check_trend_template(df: pd.DataFrame) -> pd.Series:
    """Mark Weinstein Stage 2 'trend template' criteria.

    - Price above rising 30-week MA
    - 30-week MA rising for at least 1 week
    - Price above 50-day and 200-day SMA
    """
    close = df["close"]
    sma_150 = close.rolling(window=150).mean()  # ~30 weeks
    sma_50 = close.rolling(window=50).mean()
    sma_200 = close.rolling(window=200).mean()

    above_150 = close > sma_150
    ma_rising = sma_150 > sma_150.shift(5)
    above_50 = close > sma_50
    above_200 = close > sma_200

    return above_150 & ma_rising & above_50 & above_200


def compute_all_patterns(df: pd.DataFrame) -> pd.DataFrame:
    """Run all pattern detection on OHLCV dataframe."""
    result = df.copy()

    breakout = detect_consolidation_breakout(df)
    result["breakout_detected"] = breakout["breakout_detected"]
    result["volume_expansion"] = breakout["volume_expansion"]

    trend = detect_trend_structure(df)
    result["higher_high"] = trend["higher_high"]
    result["higher_low"] = trend["higher_low"]

    gap = detect_gap(df)
    result["gap_up"] = gap["gap_up"]
    result["gap_pct"] = gap["gap_pct"]

    result["trend_template_pass"] = check_trend_template(df)

    support, resistance = compute_support_resistance(df)
    current_price = df["close"].iloc[-1] if len(df) > 0 else 0
    result["nearest_support"] = min(support, default=None, key=lambda s: abs(current_price - s))
    result["nearest_resistance"] = min(
        resistance, default=None, key=lambda r: abs(current_price - r)
    )

    return result
