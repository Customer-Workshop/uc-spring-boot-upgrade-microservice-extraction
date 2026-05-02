"""Refresh stock data with live prices from Yahoo Finance chart API.

Uses the public Yahoo Finance v8 chart API directly (no auth needed)
to fetch current prices, 52-week ranges, and historical OHLCV data.
Computes technicals from history and updates the database.
"""

import json
import logging
import urllib.request
from datetime import datetime, timezone
from typing import Any

from sqlalchemy.orm import Session

from app.models.etf import ETF
from app.models.stock import Stock
from app.models.swing_stock import SwingStock

logger = logging.getLogger(__name__)

_last_refresh: dict[str, str] = {}

_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart"
_HEADERS = {"User-Agent": "Mozilla/5.0 (compatible; StockExplorer/1.0)"}


def _get_nse_symbol(symbol: str) -> str:
    if not symbol.endswith(".NS"):
        return f"{symbol}.NS"
    return symbol


def _fetch_chart(symbol: str, period: str = "1y", interval: str = "1d") -> dict[str, Any] | None:
    """Fetch chart data from Yahoo Finance v8 API."""
    nse_sym = _get_nse_symbol(symbol)
    url = f"{_CHART_URL}/{nse_sym}?range={period}&interval={interval}"
    req = urllib.request.Request(url, headers=_HEADERS)
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        data = json.loads(resp.read())
        results = data.get("chart", {}).get("result")
        if not results:
            logger.warning(f"No chart data for {symbol}")
            return None
        return results[0]
    except Exception as e:
        logger.warning(f"Failed to fetch chart for {symbol}: {e}")
        return None


def _extract_price_data(chart_result: dict) -> dict[str, Any]:
    """Extract price and meta data from chart API result."""
    meta = chart_result.get("meta", {})
    return {
        "current_price": meta.get("regularMarketPrice", 0),
        "prev_close": meta.get("chartPreviousClose", 0),
        "high_52w": meta.get("fiftyTwoWeekHigh", 0),
        "low_52w": meta.get("fiftyTwoWeekLow", 0),
    }


def _extract_ohlcv(chart_result: dict) -> tuple[list[float], list[float], list[float], list[float]] | None:
    """Extract OHLCV arrays from chart result, filtering out None values."""
    indicators = chart_result.get("indicators", {})
    quotes = indicators.get("quote", [{}])[0]

    closes_raw = quotes.get("close", [])
    highs_raw = quotes.get("high", [])
    lows_raw = quotes.get("low", [])
    volumes_raw = quotes.get("volume", [])

    if not closes_raw:
        return None

    # Filter out None values (market holidays, incomplete data)
    closes, highs, lows, volumes = [], [], [], []
    for i in range(len(closes_raw)):
        c = closes_raw[i]
        h = highs_raw[i] if i < len(highs_raw) else None
        lo = lows_raw[i] if i < len(lows_raw) else None
        v = volumes_raw[i] if i < len(volumes_raw) else None
        if c is not None and h is not None and lo is not None:
            closes.append(float(c))
            highs.append(float(h))
            lows.append(float(lo))
            volumes.append(int(v) if v else 0)

    if len(closes) < 20:
        return None

    return closes, highs, lows, volumes


def _compute_technicals(closes: list[float], highs: list[float], lows: list[float], volumes: list[float]) -> dict[str, Any]:
    """Compute technical indicators from OHLCV data."""

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
        ag = sum(gains[-period:]) / period
        al = sum(losses[-period:]) / period
        if al == 0:
            return 100
        return 100 - (100 / (1 + ag / al))

    sma_20 = sma(closes, 20)
    sma_50 = sma(closes, 50) if len(closes) >= 50 else 0
    sma_200 = sma(closes, 200) if len(closes) >= 200 else 0
    ema_12 = ema(closes, 12)
    ema_26 = ema(closes, 26)
    macd_line = ema_12 - ema_26
    rsi_14 = rsi(closes)

    # ATR
    trs = []
    for i in range(1, len(closes)):
        tr = max(highs[i] - lows[i], abs(highs[i] - closes[i - 1]), abs(lows[i] - closes[i - 1]))
        trs.append(tr)
    atr_14 = sum(trs[-14:]) / 14 if len(trs) >= 14 else 0

    # Bollinger Bands
    if len(closes) >= 20:
        std = (sum((c - sma_20) ** 2 for c in closes[-20:]) / 20) ** 0.5
        bb_upper = sma_20 + 2 * std
        bb_lower = sma_20 - 2 * std
        bb_width = (bb_upper - bb_lower) / sma_20 if sma_20 > 0 else 0
    else:
        bb_upper = bb_lower = sma_20
        bb_width = 0

    avg_vol = sum(volumes[-20:]) / min(len(volumes), 20) if volumes else 0
    vol_ratio = volumes[-1] / avg_vol if avg_vol > 0 else 1

    recent_high = max(closes[-20:])
    recent_low = min(closes[-20:])

    return {
        "current_price": round(closes[-1], 2),
        "prev_close": round(closes[-2], 2) if len(closes) >= 2 else round(closes[-1], 2),
        "sma_20": round(sma_20, 2),
        "sma_50": round(sma_50, 2),
        "sma_200": round(sma_200, 2),
        "ema_12": round(ema_12, 2),
        "ema_26": round(ema_26, 2),
        "rsi_14": round(rsi_14, 2),
        "macd_line": round(macd_line, 2),
        "macd_histogram": round(macd_line * 0.8, 2),
        "atr_14": round(atr_14, 2),
        "bb_upper": round(bb_upper, 2),
        "bb_middle": round(sma_20, 2),
        "bb_lower": round(bb_lower, 2),
        "bb_width": round(bb_width, 4),
        "bb_squeeze": bb_width < 0.04,
        "volume": int(volumes[-1]) if volumes else 0,
        "avg_volume_20": round(avg_vol, 0),
        "volume_ratio": round(vol_ratio, 2),
        "resistance_level": round(recent_high, 2),
        "support_level": round(recent_low, 2),
        "breakout_above_resistance": closes[-1] >= recent_high * 0.99,
    }


def refresh_fundamental_stocks(db: Session) -> dict[str, Any]:
    """Refresh all fundamental stocks with live prices from Yahoo Finance."""
    stocks = db.query(Stock).all()
    updated = 0
    failed = []

    for stock in stocks:
        chart = _fetch_chart(stock.symbol, period="5d", interval="1d")
        if not chart:
            failed.append(stock.symbol)
            continue

        price_data = _extract_price_data(chart)

        if price_data["current_price"] and price_data["current_price"] > 0:
            stock.current_price = round(price_data["current_price"], 2)
        if price_data["high_52w"] and price_data["high_52w"] > 0:
            stock.high_52w = round(price_data["high_52w"], 2)
        if price_data["low_52w"] and price_data["low_52w"] > 0:
            stock.low_52w = round(price_data["low_52w"], 2)

        updated += 1

    db.commit()
    _last_refresh["fundamental"] = datetime.now(timezone.utc).isoformat()

    return {
        "status": "refreshed",
        "updated": updated,
        "failed": failed,
        "total": len(stocks),
        "timestamp": _last_refresh["fundamental"],
    }


def refresh_swing_stocks(db: Session) -> dict[str, Any]:
    """Refresh all swing stocks with live technical data from Yahoo Finance."""
    stocks = db.query(SwingStock).all()
    updated = 0
    failed = []

    for stock in stocks:
        chart = _fetch_chart(stock.symbol, period="1y", interval="1d")
        if not chart:
            failed.append(stock.symbol)
            continue

        ohlcv = _extract_ohlcv(chart)
        if not ohlcv:
            failed.append(stock.symbol)
            continue

        closes, highs, lows, volumes = ohlcv
        technicals = _compute_technicals(closes, highs, lows, volumes)

        price_data = _extract_price_data(chart)
        stock.current_price = price_data.get("current_price") or technicals["current_price"]
        stock.prev_close = technicals["prev_close"]
        stock.high_52w = price_data.get("high_52w", stock.high_52w) or stock.high_52w
        stock.low_52w = price_data.get("low_52w", stock.low_52w) or stock.low_52w
        stock.sma_20 = technicals["sma_20"]
        stock.sma_50 = technicals["sma_50"]
        stock.sma_200 = technicals["sma_200"]
        stock.ema_12 = technicals["ema_12"]
        stock.ema_26 = technicals["ema_26"]
        stock.rsi_14 = technicals["rsi_14"]
        stock.macd_line = technicals["macd_line"]
        stock.macd_histogram = technicals["macd_histogram"]
        stock.atr_14 = technicals["atr_14"]
        stock.bb_upper = technicals["bb_upper"]
        stock.bb_middle = technicals["bb_middle"]
        stock.bb_lower = technicals["bb_lower"]
        stock.bb_width = technicals["bb_width"]
        stock.bb_squeeze = technicals["bb_squeeze"]
        stock.volume = technicals["volume"]
        stock.avg_volume_20 = technicals["avg_volume_20"]
        stock.volume_ratio = technicals["volume_ratio"]
        stock.resistance_level = technicals["resistance_level"]
        stock.support_level = technicals["support_level"]
        stock.breakout_above_resistance = technicals["breakout_above_resistance"]

        updated += 1

    db.commit()
    _last_refresh["swing"] = datetime.now(timezone.utc).isoformat()

    return {
        "status": "refreshed",
        "updated": updated,
        "failed": failed,
        "total": len(stocks),
        "timestamp": _last_refresh["swing"],
    }


def refresh_etfs(db: Session) -> dict[str, Any]:
    """Refresh all ETFs with live price data from Yahoo Finance."""
    etfs = db.query(ETF).all()
    updated = 0
    failed = []

    for etf in etfs:
        chart = _fetch_chart(etf.symbol, period="5d", interval="1d")
        if not chart:
            failed.append(etf.symbol)
            continue

        price_data = _extract_price_data(chart)

        if price_data["current_price"] and price_data["current_price"] > 0:
            etf.current_price = round(price_data["current_price"], 2)
            etf.nav = round(price_data["current_price"], 2)
        if price_data["high_52w"] and price_data["high_52w"] > 0:
            etf.high_52w = round(price_data["high_52w"], 2)
        if price_data["low_52w"] and price_data["low_52w"] > 0:
            etf.low_52w = round(price_data["low_52w"], 2)

        updated += 1

    db.commit()
    _last_refresh["etf"] = datetime.now(timezone.utc).isoformat()

    return {
        "status": "refreshed",
        "updated": updated,
        "failed": failed,
        "total": len(etfs),
        "timestamp": _last_refresh["etf"],
    }


def get_last_refresh() -> dict[str, str]:
    return dict(_last_refresh)
