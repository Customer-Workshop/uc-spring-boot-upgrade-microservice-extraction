"""Real-time market data fetcher using Yahoo Finance (yfinance).

Fetches live prices, technicals, and fundamentals for Indian stocks.
Falls back to mock data if API fails or rate-limited.
"""

import logging
from typing import Any

logger = logging.getLogger(__name__)

try:
    import yfinance as yf
    HAS_YFINANCE = True
except ImportError:
    HAS_YFINANCE = False
    logger.warning("yfinance not installed — using mock data only")


def get_nse_symbol(symbol: str) -> str:
    """Convert plain symbol to NSE Yahoo Finance format."""
    if not symbol.endswith(".NS"):
        return f"{symbol}.NS"
    return symbol


def fetch_live_price(symbol: str) -> dict[str, Any] | None:
    """Fetch live price data for an Indian stock."""
    if not HAS_YFINANCE:
        return None
    try:
        ticker = yf.Ticker(get_nse_symbol(symbol))
        info = ticker.info
        if not info or "regularMarketPrice" not in info:
            return None
        return {
            "current_price": info.get("regularMarketPrice", 0),
            "prev_close": info.get("previousClose", 0),
            "high_52w": info.get("fiftyTwoWeekHigh", 0),
            "low_52w": info.get("fiftyTwoWeekLow", 0),
            "market_cap_cr": round(info.get("marketCap", 0) / 1e7, 0),
            "volume": info.get("volume", 0),
            "avg_volume": info.get("averageVolume", 0),
            "pe_ratio": info.get("trailingPE", 0) or 0,
            "pb_ratio": info.get("priceToBook", 0) or 0,
            "eps": info.get("trailingEps", 0) or 0,
            "book_value": info.get("bookValue", 0) or 0,
            "dividend_yield": (info.get("dividendYield", 0) or 0) * 100,
            "roe": (info.get("returnOnEquity", 0) or 0) * 100,
            "debt_to_equity": info.get("debtToEquity", 0) or 0,
            "sector": info.get("sector", ""),
            "industry": info.get("industry", ""),
            "name": info.get("longName", symbol),
        }
    except Exception as e:
        logger.warning(f"Failed to fetch live data for {symbol}: {e}")
        return None


def fetch_stock_history(symbol: str, period: str = "1y") -> list[dict] | None:
    """Fetch historical OHLCV data."""
    if not HAS_YFINANCE:
        return None
    try:
        ticker = yf.Ticker(get_nse_symbol(symbol))
        hist = ticker.history(period=period)
        if hist.empty:
            return None
        records = []
        for date, row in hist.iterrows():
            records.append({
                "date": date.strftime("%Y-%m-%d"),
                "open": round(row["Open"], 2),
                "high": round(row["High"], 2),
                "low": round(row["Low"], 2),
                "close": round(row["Close"], 2),
                "volume": int(row["Volume"]),
            })
        return records
    except Exception as e:
        logger.warning(f"Failed to fetch history for {symbol}: {e}")
        return None


def compute_technicals_from_history(records: list[dict]) -> dict[str, Any]:
    """Compute technical indicators from OHLCV data."""
    if not records or len(records) < 20:
        return {}

    closes = [r["close"] for r in records]
    volumes = [r["volume"] for r in records]

    def sma(data: list, period: int) -> float:
        if len(data) < period:
            return 0
        return sum(data[-period:]) / period

    def ema(data: list, period: int) -> float:
        if len(data) < period:
            return 0
        k = 2 / (period + 1)
        val = sma(data[:period], period)
        for price in data[period:]:
            val = price * k + val * (1 - k)
        return val

    def rsi(data: list, period: int = 14) -> float:
        if len(data) < period + 1:
            return 50
        gains, losses = [], []
        for i in range(1, len(data)):
            diff = data[i] - data[i - 1]
            gains.append(max(diff, 0))
            losses.append(max(-diff, 0))
        avg_gain = sum(gains[-period:]) / period
        avg_loss = sum(losses[-period:]) / period
        if avg_loss == 0:
            return 100
        rs = avg_gain / avg_loss
        return 100 - (100 / (1 + rs))

    def atr(records: list[dict], period: int = 14) -> float:
        if len(records) < period + 1:
            return 0
        trs = []
        for i in range(1, len(records)):
            h = records[i]["high"]
            l = records[i]["low"]
            pc = records[i - 1]["close"]
            tr = max(h - l, abs(h - pc), abs(l - pc))
            trs.append(tr)
        return sum(trs[-period:]) / period

    sma_20 = sma(closes, 20)
    sma_50 = sma(closes, 50) if len(closes) >= 50 else 0
    sma_200 = sma(closes, 200) if len(closes) >= 200 else 0
    ema_12 = ema(closes, 12)
    ema_26 = ema(closes, 26)
    macd_line = ema_12 - ema_26
    rsi_14 = rsi(closes, 14)
    atr_14 = atr(records, 14)

    # Bollinger Bands
    if len(closes) >= 20:
        mean = sma_20
        std = (sum((c - mean) ** 2 for c in closes[-20:]) / 20) ** 0.5
        bb_upper = mean + 2 * std
        bb_lower = mean - 2 * std
        bb_width = (bb_upper - bb_lower) / mean if mean > 0 else 0
    else:
        bb_upper = bb_lower = sma_20
        bb_width = 0

    avg_vol = sum(volumes[-20:]) / min(len(volumes), 20) if volumes else 0
    vol_ratio = volumes[-1] / avg_vol if avg_vol > 0 else 1

    return {
        "sma_20": round(sma_20, 2),
        "sma_50": round(sma_50, 2),
        "sma_200": round(sma_200, 2),
        "ema_12": round(ema_12, 2),
        "ema_26": round(ema_26, 2),
        "macd_line": round(macd_line, 2),
        "rsi_14": round(rsi_14, 2),
        "atr_14": round(atr_14, 2),
        "bb_upper": round(bb_upper, 2),
        "bb_lower": round(bb_lower, 2),
        "bb_middle": round(sma_20, 2),
        "bb_width": round(bb_width, 4),
        "volume_ratio": round(vol_ratio, 2),
        "current_volume": volumes[-1] if volumes else 0,
        "avg_volume_20": round(avg_vol, 0),
    }


def fetch_live_stock_data(symbol: str) -> dict[str, Any] | None:
    """Fetch complete live data: price + technicals."""
    price_data = fetch_live_price(symbol)
    if not price_data:
        return None

    history = fetch_stock_history(symbol, "1y")
    technicals = compute_technicals_from_history(history) if history else {}

    return {**price_data, **technicals}
