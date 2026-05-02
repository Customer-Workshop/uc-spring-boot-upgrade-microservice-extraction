"""Technical Swing Trading — Confirmation Ladder Scoring Engine.

5-signal confirmation stack (2 points each, max 10):
  A. Trend filter (MA alignment)
  B. Momentum (RSI + MACD)
  C. Volume confirmation
  D. Volatility (Bollinger squeeze/expansion)
  E. Structure (breakout above resistance)

Trade setup: entry, stop-loss (ATR-based), targets (R-multiples).
"""

from sqlalchemy.orm import Session

from app.models.swing_stock import SwingScore, SwingStock


def evaluate_trend(stock: SwingStock) -> tuple[int, str]:
    above_50 = stock.current_price > stock.sma_50
    above_200 = stock.current_price > stock.sma_200
    ma_aligned = stock.sma_20 > stock.sma_50

    if above_50 and above_200 and ma_aligned:
        return 2, f"Strong uptrend: Price above 50 & 200 MA, MAs aligned (20>{50}>{200})"
    elif above_50 and above_200:
        return 2, f"Uptrend: Price ₹{stock.current_price:.0f} above 50 MA (₹{stock.sma_50:.0f}) & 200 MA (₹{stock.sma_200:.0f})"
    elif above_50:
        return 1, f"Medium-term uptrend: Above 50 MA (₹{stock.sma_50:.0f}) but below 200 MA (₹{stock.sma_200:.0f})"
    else:
        return 0, f"Downtrend: Price ₹{stock.current_price:.0f} below 50 MA (₹{stock.sma_50:.0f})"


def evaluate_momentum(stock: SwingStock) -> tuple[int, str]:
    rsi_strong = stock.rsi_14 > 50
    rsi_very_strong = stock.rsi_14 > 55
    macd_bullish = stock.macd_histogram > 0
    macd_crossover = stock.macd_line > stock.macd_signal

    parts = []
    score = 0

    if rsi_very_strong and macd_bullish and macd_crossover:
        score = 2
        parts.append(f"RSI {stock.rsi_14:.1f} (strong)")
        parts.append("MACD bullish crossover + positive histogram")
    elif rsi_strong and (macd_bullish or macd_crossover):
        score = 1
        parts.append(f"RSI {stock.rsi_14:.1f}")
        if macd_bullish:
            parts.append("MACD histogram positive")
        if macd_crossover:
            parts.append("MACD bullish crossover")
    else:
        if not rsi_strong:
            parts.append(f"RSI {stock.rsi_14:.1f} (weak)")
        if not macd_bullish:
            parts.append("MACD histogram negative")

    return score, "; ".join(parts)


def evaluate_volume(stock: SwingStock) -> tuple[int, str]:
    ratio = stock.volume_ratio
    if ratio >= 1.5:
        return 2, f"Volume surge: {ratio:.1f}x average — strong institutional interest"
    elif ratio >= 1.2:
        return 1, f"Volume above average: {ratio:.1f}x — moderate confirmation"
    else:
        return 0, f"Volume below average: {ratio:.1f}x — weak participation"


def evaluate_volatility(stock: SwingStock) -> tuple[int, str]:
    if stock.bb_squeeze and stock.current_price > stock.bb_upper:
        return 2, f"Bollinger squeeze breakout! Price ₹{stock.current_price:.0f} broke above upper band ₹{stock.bb_upper:.0f}"
    elif stock.current_price > stock.bb_middle and stock.bb_width < 0.1:
        return 1, f"Tight consolidation, price above middle band — potential expansion ahead"
    elif stock.current_price > stock.bb_middle:
        return 1, f"Price above middle Bollinger band (₹{stock.bb_middle:.0f}), BB width: {stock.bb_width:.2f}"
    else:
        return 0, f"Price below middle band, BB width: {stock.bb_width:.2f} — no squeeze setup"


def evaluate_structure(stock: SwingStock) -> tuple[int, str]:
    if stock.breakout_above_resistance and stock.volume_ratio >= 1.2:
        return 2, f"Confirmed breakout above resistance ₹{stock.resistance_level:.0f} with volume"
    elif stock.breakout_above_resistance:
        return 1, f"Breakout above resistance ₹{stock.resistance_level:.0f} but volume needs confirmation"
    elif stock.current_price > stock.support_level:
        return 0, f"Above support ₹{stock.support_level:.0f}, resistance at ₹{stock.resistance_level:.0f} — no breakout yet"
    else:
        return 0, f"Below support ₹{stock.support_level:.0f} — breakdown risk"


def compute_trade_setup(stock: SwingStock, total_score: int) -> dict:
    entry = stock.current_price
    atr_stop = entry - (1.5 * stock.atr_14)
    structure_stop = stock.support_level * 0.99 if stock.support_level > 0 else atr_stop
    stop_loss = max(atr_stop, structure_stop)

    risk = entry - stop_loss
    if risk <= 0:
        risk = stock.atr_14

    target_1 = entry + risk
    target_2 = entry + (2 * risk)
    rr = (target_2 - entry) / risk if risk > 0 else 0

    if total_score >= 8:
        signal = "BUY"
        verdict = "Best Candidate — All confirmations aligned"
    elif total_score >= 6:
        signal = "WATCH"
        verdict = "Watchlist — Wait for next candle confirmation"
    else:
        signal = "SKIP"
        verdict = "Skip — Insufficient confirmations"

    return {
        "signal": signal,
        "entry_price": round(entry, 2),
        "stop_loss": round(stop_loss, 2),
        "target_1": round(target_1, 2),
        "target_2": round(target_2, 2),
        "risk_reward": round(rr, 2),
        "verdict": verdict,
    }


def score_swing_stock(stock: SwingStock) -> dict:
    trend_score, trend_detail = evaluate_trend(stock)
    mom_score, mom_detail = evaluate_momentum(stock)
    vol_score, vol_detail = evaluate_volume(stock)
    volatility_score, volatility_detail = evaluate_volatility(stock)
    struct_score, struct_detail = evaluate_structure(stock)

    total = trend_score + mom_score + vol_score + volatility_score + struct_score
    setup = compute_trade_setup(stock, total)

    return {
        "stock_id": stock.id,
        "symbol": stock.symbol,
        "trend_score": trend_score, "trend_detail": trend_detail,
        "momentum_score": mom_score, "momentum_detail": mom_detail,
        "volume_score": vol_score, "volume_detail": vol_detail,
        "volatility_score": volatility_score, "volatility_detail": volatility_detail,
        "structure_score": struct_score, "structure_detail": struct_detail,
        "total_score": total,
        **setup,
    }


def score_all_swing_stocks(db: Session) -> int:
    stocks = db.query(SwingStock).all()
    count = 0
    for stock in stocks:
        data = score_swing_stock(stock)
        existing = db.query(SwingScore).filter(SwingScore.stock_id == stock.id).first()
        if existing:
            for k, v in data.items():
                setattr(existing, k, v)
        else:
            db.add(SwingScore(**data))
        count += 1
    db.commit()
    return count
