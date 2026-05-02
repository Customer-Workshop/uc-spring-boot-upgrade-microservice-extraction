"""ETF Screener — Best ETFs to invest using weekly/monthly MA and other factors.

5-factor scoring (2 points each, max 10):
  A. Trend (price vs weekly/monthly MAs)
  B. Momentum (RSI + returns)
  C. Cost efficiency (expense ratio + tracking error)
  D. Liquidity (AUM + avg volume)
  E. Performance (returns vs category)
"""

from sqlalchemy.orm import Session

from app.models.etf import ETF, ETFScore


def evaluate_etf_trend(etf: ETF) -> tuple[int, str]:
    above_20w = etf.above_20w_ma == 1
    above_50w = etf.above_50w_ma == 1

    if above_20w and above_50w:
        return 2, f"Strong uptrend: Price above 20W MA (₹{etf.sma_20w:.0f}) & 50W MA (₹{etf.sma_50w:.0f})"
    elif above_20w:
        return 1, f"Short-term uptrend: Above 20W MA but below 50W MA"
    else:
        return 0, f"Downtrend: Below key weekly moving averages"


def evaluate_etf_momentum(etf: ETF) -> tuple[int, str]:
    rsi_ok = 40 < etf.rsi_14 < 70
    returns_positive = etf.return_3m > 0 and etf.return_6m > 0

    if rsi_ok and returns_positive and etf.return_1y > 10:
        return 2, f"Strong momentum: RSI {etf.rsi_14:.0f}, 3M return {etf.return_3m:.1f}%, 1Y return {etf.return_1y:.1f}%"
    elif rsi_ok and etf.return_3m > 0:
        return 1, f"Moderate momentum: RSI {etf.rsi_14:.0f}, 3M return {etf.return_3m:.1f}%"
    else:
        return 0, f"Weak momentum: RSI {etf.rsi_14:.0f}, 3M return {etf.return_3m:.1f}%"


def evaluate_etf_cost(etf: ETF) -> tuple[int, str]:
    low_expense = etf.expense_ratio <= 0.20
    low_tracking = etf.tracking_error <= 0.50

    if low_expense and low_tracking:
        return 2, f"Excellent cost: Expense {etf.expense_ratio:.2f}%, Tracking error {etf.tracking_error:.2f}%"
    elif low_expense or etf.expense_ratio <= 0.50:
        return 1, f"Moderate cost: Expense {etf.expense_ratio:.2f}%, Tracking error {etf.tracking_error:.2f}%"
    else:
        return 0, f"High cost: Expense {etf.expense_ratio:.2f}%, Tracking error {etf.tracking_error:.2f}%"


def evaluate_etf_liquidity(etf: ETF) -> tuple[int, str]:
    high_aum = etf.aum_cr >= 5000
    good_volume = etf.avg_volume >= 100000

    if high_aum and good_volume:
        return 2, f"Highly liquid: AUM ₹{etf.aum_cr:,.0f} Cr, Avg Vol {etf.avg_volume:,.0f}"
    elif high_aum or good_volume:
        return 1, f"Moderate liquidity: AUM ₹{etf.aum_cr:,.0f} Cr, Avg Vol {etf.avg_volume:,.0f}"
    else:
        return 0, f"Low liquidity: AUM ₹{etf.aum_cr:,.0f} Cr, Avg Vol {etf.avg_volume:,.0f}"


def evaluate_etf_performance(etf: ETF) -> tuple[int, str]:
    strong_3y = etf.return_3y_cagr > 12
    strong_5y = etf.return_5y_cagr > 10

    if strong_3y and strong_5y:
        return 2, f"Strong track record: 3Y CAGR {etf.return_3y_cagr:.1f}%, 5Y CAGR {etf.return_5y_cagr:.1f}%"
    elif strong_3y or strong_5y:
        return 1, f"Moderate performance: 3Y CAGR {etf.return_3y_cagr:.1f}%, 5Y CAGR {etf.return_5y_cagr:.1f}%"
    else:
        return 0, f"Weak performance: 3Y CAGR {etf.return_3y_cagr:.1f}%, 5Y CAGR {etf.return_5y_cagr:.1f}%"


def score_etf(etf: ETF) -> dict:
    trend_s, trend_d = evaluate_etf_trend(etf)
    mom_s, mom_d = evaluate_etf_momentum(etf)
    cost_s, cost_d = evaluate_etf_cost(etf)
    liq_s, liq_d = evaluate_etf_liquidity(etf)
    perf_s, perf_d = evaluate_etf_performance(etf)

    total = trend_s + mom_s + cost_s + liq_s + perf_s

    if total >= 8:
        verdict = "Best Pick"
    elif total >= 6:
        verdict = "Good Choice"
    elif total >= 4:
        verdict = "Average"
    else:
        verdict = "Below Average"

    return {
        "etf_id": etf.id,
        "symbol": etf.symbol,
        "trend_score": trend_s, "trend_detail": trend_d,
        "momentum_score": mom_s, "momentum_detail": mom_d,
        "cost_score": cost_s, "cost_detail": cost_d,
        "liquidity_score": liq_s, "liquidity_detail": liq_d,
        "performance_score": perf_s, "performance_detail": perf_d,
        "total_score": total,
        "verdict": verdict,
    }


def score_all_etfs(db: Session) -> int:
    etfs = db.query(ETF).all()
    count = 0
    for etf in etfs:
        data = score_etf(etf)
        existing = db.query(ETFScore).filter(ETFScore.etf_id == etf.id).first()
        if existing:
            for k, v in data.items():
                setattr(existing, k, v)
        else:
            db.add(ETFScore(**data))
        count += 1
    db.commit()
    return count
