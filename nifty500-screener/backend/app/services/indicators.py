"""Technical indicator calculations using pandas + ta library."""

import numpy as np
import pandas as pd
from ta.momentum import RSIIndicator
from ta.trend import MACD, SMAIndicator, EMAIndicator
from ta.volatility import BollingerBands, AverageTrueRange


def compute_rsi(close: pd.Series, window: int = 14) -> pd.Series:
    return RSIIndicator(close=close, window=window).rsi()


def compute_sma(close: pd.Series, window: int = 20) -> pd.Series:
    return SMAIndicator(close=close, window=window).sma_indicator()


def compute_ema(close: pd.Series, window: int = 20) -> pd.Series:
    return EMAIndicator(close=close, window=window).ema_indicator()


def compute_macd(
    close: pd.Series,
    window_slow: int = 26,
    window_fast: int = 12,
    window_sign: int = 9,
) -> tuple[pd.Series, pd.Series, pd.Series]:
    macd = MACD(
        close=close,
        window_slow=window_slow,
        window_fast=window_fast,
        window_sign=window_sign,
    )
    return macd.macd(), macd.macd_signal(), macd.macd_diff()


def compute_bollinger_bands(
    close: pd.Series, window: int = 20, window_dev: int = 2
) -> tuple[pd.Series, pd.Series, pd.Series, pd.Series]:
    bb = BollingerBands(close=close, window=window, window_dev=window_dev)
    upper = bb.bollinger_hband()
    middle = bb.bollinger_mavg()
    lower = bb.bollinger_lband()
    width = bb.bollinger_wband()
    return upper, middle, lower, width


def compute_atr(
    high: pd.Series, low: pd.Series, close: pd.Series, window: int = 14
) -> pd.Series:
    return AverageTrueRange(high=high, low=low, close=close, window=window).average_true_range()


def detect_ma_crossover(sma_short: pd.Series, sma_long: pd.Series) -> pd.Series:
    prev_short = sma_short.shift(1)
    prev_long = sma_long.shift(1)
    golden = (prev_short <= prev_long) & (sma_short > sma_long)
    death = (prev_short >= prev_long) & (sma_short < sma_long)
    result = pd.Series("none", index=sma_short.index)
    result[golden] = "golden_cross"
    result[death] = "death_cross"
    return result


def compute_relative_strength(
    stock_close: pd.Series, benchmark_close: pd.Series, window: int = 50
) -> pd.Series:
    if len(stock_close) == 0 or len(benchmark_close) == 0:
        return pd.Series(dtype=float)
    ratio = stock_close / benchmark_close
    rs = ratio / ratio.rolling(window=window).mean()
    return rs


def compute_all_technicals(df: pd.DataFrame) -> pd.DataFrame:
    """Compute all technical indicators for a stock OHLCV dataframe.

    Expects columns: date, open, high, low, close, volume.
    Returns dataframe with all indicator columns added.
    """
    result = df.copy()
    close = result["close"]
    high = result["high"]
    low = result["low"]

    result["rsi_14"] = compute_rsi(close, 14)
    result["sma_20"] = compute_sma(close, 20)
    result["sma_50"] = compute_sma(close, 50)
    result["sma_200"] = compute_sma(close, 200)
    result["ema_20"] = compute_ema(close, 20)
    result["ema_50"] = compute_ema(close, 50)

    macd_line, macd_signal, macd_hist = compute_macd(close)
    result["macd_line"] = macd_line
    result["macd_signal"] = macd_signal
    result["macd_histogram"] = macd_hist

    bb_upper, bb_middle, bb_lower, bb_width = compute_bollinger_bands(close)
    result["bb_upper"] = bb_upper
    result["bb_middle"] = bb_middle
    result["bb_lower"] = bb_lower
    result["bb_width"] = bb_width

    bb_width_ma = bb_width.rolling(window=120).mean()
    result["bb_squeeze"] = bb_width < bb_width_ma

    result["atr_14"] = compute_atr(high, low, close, 14)
    result["volatility_pct"] = result["atr_14"] / close * 100

    result["ma_crossover"] = detect_ma_crossover(result["sma_50"], result["sma_200"])

    sma200 = result["sma_200"]
    result["distance_from_200sma"] = ((close - sma200) / sma200 * 100).where(sma200 > 0)
    result["sma_200_slope"] = sma200.diff(20) / sma200.shift(20) * 100

    return result
