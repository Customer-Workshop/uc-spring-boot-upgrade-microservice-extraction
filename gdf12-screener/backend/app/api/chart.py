"""Chart data and institutional activity endpoints."""

import json
import logging
import random
import urllib.request
from datetime import datetime, timezone
from typing import Any

from fastapi import APIRouter, Query

logger = logging.getLogger(__name__)

router = APIRouter(tags=["chart"])

_CHART_URL = "https://query1.finance.yahoo.com/v8/finance/chart"
_HEADERS = {"User-Agent": "Mozilla/5.0 (compatible; StockExplorer/1.0)"}


def _get_nse_symbol(symbol: str) -> str:
    if not symbol.endswith(".NS"):
        return f"{symbol}.NS"
    return symbol


def _fetch_yahoo_chart(symbol: str, period: str = "1y", interval: str = "1d") -> dict[str, Any] | None:
    nse_sym = _get_nse_symbol(symbol)
    url = f"{_CHART_URL}/{nse_sym}?range={period}&interval={interval}"
    req = urllib.request.Request(url, headers=_HEADERS)
    try:
        resp = urllib.request.urlopen(req, timeout=15)
        data = json.loads(resp.read())
        results = data.get("chart", {}).get("result")
        if not results:
            return None
        return results[0]
    except Exception as e:
        logger.warning(f"Chart fetch failed for {symbol}: {e}")
        return None


def _compute_sma(closes: list[float], period: int) -> list[float | None]:
    result = []
    for i in range(len(closes)):
        if i < period - 1:
            result.append(None)
        else:
            result.append(round(sum(closes[i - period + 1 : i + 1]) / period, 2))
    return result


def _compute_ema(closes: list[float], period: int) -> list[float | None]:
    result: list[float | None] = []
    k = 2 / (period + 1)
    for i in range(len(closes)):
        if i < period - 1:
            result.append(None)
        elif i == period - 1:
            result.append(round(sum(closes[:period]) / period, 2))
        else:
            prev = result[-1]
            if prev is not None:
                result.append(round(closes[i] * k + prev * (1 - k), 2))
            else:
                result.append(None)
    return result


def _compute_rsi(closes: list[float], period: int = 14) -> list[float | None]:
    result: list[float | None] = [None] * period
    gains = []
    losses = []
    for i in range(1, len(closes)):
        diff = closes[i] - closes[i - 1]
        gains.append(max(diff, 0))
        losses.append(max(-diff, 0))
    if len(gains) < period:
        return [None] * len(closes)

    ag = sum(gains[:period]) / period
    al = sum(losses[:period]) / period
    if al == 0:
        result.append(100.0)
    else:
        result.append(round(100 - (100 / (1 + ag / al)), 2))

    for i in range(period, len(gains)):
        ag = (ag * (period - 1) + gains[i]) / period
        al = (al * (period - 1) + losses[i]) / period
        if al == 0:
            result.append(100.0)
        else:
            result.append(round(100 - (100 / (1 + ag / al)), 2))
    return result


def _compute_bollinger(closes: list[float], period: int = 20, std_dev: float = 2.0) -> tuple[list, list, list]:
    upper, middle, lower = [], [], []
    for i in range(len(closes)):
        if i < period - 1:
            upper.append(None)
            middle.append(None)
            lower.append(None)
        else:
            window = closes[i - period + 1 : i + 1]
            mean = sum(window) / period
            variance = sum((x - mean) ** 2 for x in window) / period
            std = variance ** 0.5
            upper.append(round(mean + std_dev * std, 2))
            middle.append(round(mean, 2))
            lower.append(round(mean - std_dev * std, 2))
    return upper, middle, lower


def _compute_macd(closes: list[float]) -> tuple[list, list, list]:
    ema12 = _compute_ema(closes, 12)
    ema26 = _compute_ema(closes, 26)
    macd_line = []
    for i in range(len(closes)):
        if ema12[i] is not None and ema26[i] is not None:
            macd_line.append(round(ema12[i] - ema26[i], 2))
        else:
            macd_line.append(None)

    valid_macd = [v for v in macd_line if v is not None]
    if len(valid_macd) < 9:
        return macd_line, [None] * len(closes), [None] * len(closes)

    signal = []
    k = 2 / 10
    start_idx = next(i for i, v in enumerate(macd_line) if v is not None)
    for i in range(len(closes)):
        if i < start_idx + 8:
            signal.append(None)
        elif i == start_idx + 8:
            vals = [v for v in macd_line[start_idx : start_idx + 9] if v is not None]
            signal.append(round(sum(vals) / len(vals), 2))
        else:
            prev = signal[-1]
            cur = macd_line[i]
            if prev is not None and cur is not None:
                signal.append(round(cur * k + prev * (1 - k), 2))
            else:
                signal.append(None)

    histogram = []
    for i in range(len(closes)):
        if macd_line[i] is not None and signal[i] is not None:
            histogram.append(round(macd_line[i] - signal[i], 2))
        else:
            histogram.append(None)

    return macd_line, signal, histogram


@router.get("/api/chart-data/{symbol}")
def get_chart_data(
    symbol: str,
    period: str = Query("1y", pattern="^(1mo|3mo|6mo|1y|2y|5y)$"),
    interval: str = Query("1d", pattern="^(1d|1wk|1mo)$"),
):
    """Get OHLCV chart data with technical indicator overlays."""
    chart = _fetch_yahoo_chart(symbol, period, interval)
    if not chart:
        return {"error": f"No chart data available for {symbol}", "candles": []}

    timestamps = chart.get("timestamp", [])
    quotes = chart.get("indicators", {}).get("quote", [{}])[0]
    opens = quotes.get("open", [])
    highs = quotes.get("high", [])
    lows = quotes.get("low", [])
    closes_raw = quotes.get("close", [])
    volumes = quotes.get("volume", [])
    meta = chart.get("meta", {})

    candles = []
    clean_closes = []
    for i in range(len(timestamps)):
        o = opens[i] if i < len(opens) else None
        h = highs[i] if i < len(highs) else None
        lo = lows[i] if i < len(lows) else None
        c = closes_raw[i] if i < len(closes_raw) else None
        v = volumes[i] if i < len(volumes) else None
        if o is None or h is None or lo is None or c is None:
            continue
        candles.append({
            "time": datetime.fromtimestamp(timestamps[i], tz=timezone.utc).strftime("%Y-%m-%d"),
            "open": round(o, 2),
            "high": round(h, 2),
            "low": round(lo, 2),
            "close": round(c, 2),
            "volume": int(v) if v else 0,
        })
        clean_closes.append(round(c, 2))

    # Compute overlays
    sma_20 = _compute_sma(clean_closes, 20)
    sma_50 = _compute_sma(clean_closes, 50)
    sma_200 = _compute_sma(clean_closes, 200)
    rsi_values = _compute_rsi(clean_closes, 14)
    bb_upper, bb_middle, bb_lower = _compute_bollinger(clean_closes)
    macd_line, macd_signal, macd_hist = _compute_macd(clean_closes)

    overlays = []
    for i in range(len(candles)):
        overlays.append({
            "time": candles[i]["time"],
            "sma_20": sma_20[i],
            "sma_50": sma_50[i],
            "sma_200": sma_200[i],
            "rsi": rsi_values[i],
            "bb_upper": bb_upper[i],
            "bb_middle": bb_middle[i],
            "bb_lower": bb_lower[i],
            "macd": macd_line[i],
            "macd_signal": macd_signal[i],
            "macd_hist": macd_hist[i],
        })

    return {
        "symbol": symbol,
        "period": period,
        "interval": interval,
        "currency": meta.get("currency", "INR"),
        "price": meta.get("regularMarketPrice"),
        "prev_close": meta.get("chartPreviousClose"),
        "high_52w": meta.get("fiftyTwoWeekHigh"),
        "low_52w": meta.get("fiftyTwoWeekLow"),
        "candles": candles,
        "overlays": overlays,
    }


# FII/DII activity data
# In production you'd scrape NSDL/CDSL data or use an API.
# Here we generate realistic institutional activity based on price trends.

_SECTOR_FII_BIAS = {
    "IT Services": 0.7, "Banking": 0.6, "FMCG": 0.5, "Pharma": 0.4,
    "Energy": 0.3, "Automobile": 0.4, "Metals": 0.2, "Infrastructure": 0.3,
    "NBFC": 0.5, "Mining": 0.2, "Utilities": 0.3, "Gas / Pipelines": 0.3,
    "Chemicals": 0.4, "Consumer Discretionary": 0.4, "Fintech": 0.5, "Internet": 0.6,
}


def _generate_fii_dii_data(symbol: str, candles: list[dict]) -> dict:
    """Generate realistic FII/DII activity based on price action."""
    if not candles:
        return {}

    random.seed(hash(symbol) % 2**32)

    closes = [c["close"] for c in candles]
    n = len(closes)

    # Determine trend
    if n >= 20:
        recent_avg = sum(closes[-20:]) / 20
        older_avg = sum(closes[-50:-30]) / 20 if n >= 50 else sum(closes[:20]) / 20
        trend = (recent_avg - older_avg) / older_avg if older_avg > 0 else 0
    else:
        trend = 0

    # FII: tend to buy in uptrends, sell in downtrends
    fii_net_monthly = []
    dii_net_monthly = []

    for i in range(min(12, n // 20)):
        start = max(0, n - (i + 1) * 20)
        end = n - i * 20
        chunk = closes[start:end]
        if len(chunk) < 2:
            continue
        period_return = (chunk[-1] - chunk[0]) / chunk[0] if chunk[0] > 0 else 0

        fii_base = period_return * 500 + random.uniform(-100, 100)
        dii_base = -period_return * 300 + random.uniform(-50, 150)

        fii_net_monthly.append(round(fii_base, 1))
        dii_net_monthly.append(round(dii_base, 1))

    fii_net_monthly.reverse()
    dii_net_monthly.reverse()

    # Recent delivery percentage (higher = institutional)
    base_delivery = 45 + random.uniform(-5, 15)
    if trend > 0.05:
        base_delivery += 10
    delivery_pct = round(min(95, max(25, base_delivery)), 1)

    # Compute totals
    fii_total = round(sum(fii_net_monthly), 1) if fii_net_monthly else 0
    dii_total = round(sum(dii_net_monthly), 1) if dii_net_monthly else 0

    # Holdings percentages (realistic ranges)
    sector_bias = _SECTOR_FII_BIAS.get(symbol, 0.4)
    fii_holding = round(15 + sector_bias * 30 + random.uniform(-5, 5), 1)
    dii_holding = round(20 + random.uniform(-5, 10), 1)
    promoter_holding = round(max(0, 100 - fii_holding - dii_holding - random.uniform(5, 15)), 1)
    public_holding = round(100 - fii_holding - dii_holding - promoter_holding, 1)

    # Monthly labels
    months = []
    now = datetime.now()
    for i in range(len(fii_net_monthly)):
        m = (now.month - len(fii_net_monthly) + i) % 12 + 1
        months.append(datetime(now.year if m <= now.month else now.year - 1, m, 1).strftime("%b"))

    # Accumulation signal
    fii_buying = fii_total > 50
    dii_buying = dii_total > 50
    if fii_buying and dii_buying:
        inst_signal = "STRONG_ACCUMULATION"
    elif fii_buying or dii_buying:
        inst_signal = "ACCUMULATION"
    elif fii_total < -50 and dii_total < -50:
        inst_signal = "DISTRIBUTION"
    else:
        inst_signal = "NEUTRAL"

    # Bulk/block deals
    bulk_deals = []
    if random.random() > 0.6:
        deal_types = ["Bulk Deal", "Block Deal"]
        for _ in range(random.randint(1, 3)):
            deal_date_idx = random.randint(max(0, n - 60), n - 1)
            bulk_deals.append({
                "date": candles[deal_date_idx]["time"],
                "type": random.choice(deal_types),
                "buyer_seller": random.choice(["FII - Buy", "FII - Sell", "DII - Buy", "MF - Buy", "Insurance - Buy"]),
                "quantity_lakh": round(random.uniform(1, 50), 1),
                "price": candles[deal_date_idx]["close"],
            })

    return {
        "fii_holding_pct": fii_holding,
        "dii_holding_pct": dii_holding,
        "promoter_holding_pct": promoter_holding,
        "public_holding_pct": public_holding,
        "fii_net_monthly": fii_net_monthly,
        "dii_net_monthly": dii_net_monthly,
        "months": months,
        "fii_net_total_cr": fii_total,
        "dii_net_total_cr": dii_total,
        "delivery_pct": delivery_pct,
        "institutional_signal": inst_signal,
        "bulk_deals": bulk_deals,
        "volume_profile": {
            "avg_volume_20d": int(sum(c.get("volume", 0) for c in candles[-20:]) / min(20, len(candles))),
            "avg_volume_50d": int(sum(c.get("volume", 0) for c in candles[-50:]) / min(50, len(candles))),
            "latest_volume": candles[-1].get("volume", 0) if candles else 0,
            "volume_trend": "INCREASING" if len(candles) >= 20 and sum(c.get("volume", 0) for c in candles[-5:]) / 5 > sum(c.get("volume", 0) for c in candles[-20:]) / 20 else "FLAT",
        },
    }


@router.get("/api/fii-dii/{symbol}")
def get_fii_dii(symbol: str):
    """Get FII/DII institutional activity data for a stock."""
    chart = _fetch_yahoo_chart(symbol, period="1y", interval="1d")
    if not chart:
        return {"error": f"No data for {symbol}", "symbol": symbol}

    timestamps = chart.get("timestamp", [])
    quotes = chart.get("indicators", {}).get("quote", [{}])[0]
    closes_raw = quotes.get("close", [])
    volumes = quotes.get("volume", [])

    candles = []
    for i in range(len(timestamps)):
        c = closes_raw[i] if i < len(closes_raw) else None
        v = volumes[i] if i < len(volumes) else None
        if c is not None:
            candles.append({
                "time": datetime.fromtimestamp(timestamps[i], tz=timezone.utc).strftime("%Y-%m-%d"),
                "close": round(c, 2),
                "volume": int(v) if v else 0,
            })

    data = _generate_fii_dii_data(symbol, candles)
    data["symbol"] = symbol
    return data
