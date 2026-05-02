"""GDF-12 (Great Defensive Fundamental – 12 Signal Model) scoring engine.

Implements the full 12-signal defensive investor framework:
  Group A – Business Quality (Non-Negotiable)
    A1. Large & Stable Company
    A2. Consistent Profits
    A3. Economic Moat
  Group B – Financial Strength (Survival Test)
    B4. Low Debt
    B5. Healthy Returns
    B6. Strong Cash Flows
  Group C – Valuation
    C7. Reasonable PE
    C8. Graham Value Check
    C9. Price to Book
  Group D – Shareholder & Market Signals
    D10. Promoter Quality
    D11. Dividend Consistency
    D12. Silent Accumulation Signal
"""

import math
from datetime import datetime, timezone

from sqlalchemy.orm import Session

from app.models.gdf_score import GDFScore
from app.models.stock import Stock


def compute_graham_number(eps: float, book_value: float) -> float:
    if eps <= 0 or book_value <= 0:
        return 0.0
    return math.sqrt(22.5 * eps * book_value)


def evaluate_a1(stock: Stock) -> tuple[bool, str]:
    passed = stock.market_cap_cr >= 5000 and stock.sales_cr >= 1000
    detail = f"Market Cap ₹{stock.market_cap_cr:,.0f} Cr, Sales ₹{stock.sales_cr:,.0f} Cr"
    if passed:
        detail += " — Large & stable ✓"
    else:
        parts = []
        if stock.market_cap_cr < 5000:
            parts.append(f"Market cap below ₹5,000 Cr threshold")
        if stock.sales_cr < 1000:
            parts.append(f"Sales below ₹1,000 Cr threshold")
        detail += f" — {'; '.join(parts)}"
    return passed, detail


def evaluate_a2(stock: Stock) -> tuple[bool, str]:
    passed = stock.profit_years_positive >= 10
    detail = f"Positive profit in {stock.profit_years_positive}/10 years"
    if passed:
        detail += " — Consistent earnings ✓"
    else:
        detail += f" — Needs 10/10 positive years"
    return passed, detail


def evaluate_a3(stock: Stock) -> tuple[bool, str]:
    moats = []
    if stock.has_strong_brand:
        moats.append("Strong brand")
    if stock.has_cost_advantage:
        moats.append("Cost advantage")
    if stock.has_regulatory_barrier:
        moats.append("Regulatory barrier")
    if stock.has_dominant_share:
        moats.append("Dominant market share")

    passed = len(moats) >= 1
    if passed:
        detail = f"Moat: {', '.join(moats)}"
        if stock.moat_description:
            detail += f" — {stock.moat_description}"
    else:
        detail = "No identifiable economic moat"
    return passed, detail


def evaluate_b4(stock: Stock) -> tuple[bool, str]:
    low_de = stock.debt_to_equity < 0.5
    high_ic = stock.interest_coverage > 4
    passed = low_de or high_ic
    detail = f"D/E: {stock.debt_to_equity:.2f}, Interest Coverage: {stock.interest_coverage:.1f}x"
    if passed:
        reasons = []
        if low_de:
            reasons.append("D/E < 0.5")
        if high_ic:
            reasons.append("IC > 4x")
        detail += f" — {', '.join(reasons)} ✓"
    else:
        detail += " — High leverage risk"
    return passed, detail


def evaluate_b5(stock: Stock) -> tuple[bool, str]:
    passed = stock.roe >= 15 and stock.roce >= 15
    detail = f"ROE: {stock.roe:.1f}%, ROCE: {stock.roce:.1f}%"
    if passed:
        detail += " — Above cost of capital ✓"
    else:
        parts = []
        if stock.roe < 15:
            parts.append("ROE < 15%")
        if stock.roce < 15:
            parts.append("ROCE < 15%")
        detail += f" — {', '.join(parts)}"
    return passed, detail


def evaluate_b6(stock: Stock) -> tuple[bool, str]:
    ocf_ok = stock.ocf_positive_years >= 8
    tracks = stock.ocf_tracks_profit
    passed = ocf_ok and tracks
    detail = f"Positive OCF in {stock.ocf_positive_years}/10 years, "
    detail += "cash tracks profit" if tracks else "cash diverges from profit"
    if passed:
        detail += " ✓"
    else:
        if not ocf_ok:
            detail += " — Inconsistent cash generation"
        else:
            detail += " — Profit quality concern"
    return passed, detail


def evaluate_c7(stock: Stock) -> tuple[bool, str]:
    pe_ok = 0 < stock.pe_ratio < 25
    below_avg = stock.pe_ratio < stock.pe_5y_avg if stock.pe_5y_avg > 0 else False
    passed = pe_ok and below_avg
    detail = f"PE: {stock.pe_ratio:.1f}, 5Y avg PE: {stock.pe_5y_avg:.1f}"
    if passed:
        detail += " — Reasonably valued ✓"
    else:
        parts = []
        if not pe_ok:
            parts.append(f"PE {'> 25 (expensive)' if stock.pe_ratio >= 25 else '<= 0 (negative earnings)'}")
        if not below_avg and stock.pe_5y_avg > 0:
            parts.append("Above 5Y average PE")
        detail += f" — {'; '.join(parts)}" if parts else ""
    return passed, detail


def evaluate_c8(stock: Stock) -> tuple[bool, str]:
    gn = compute_graham_number(stock.eps, stock.book_value)
    if gn <= 0:
        return False, f"Graham Number: N/A (EPS or Book Value ≤ 0)"

    threshold = gn * 0.70
    passed = stock.current_price <= threshold
    margin = ((gn - stock.current_price) / gn * 100) if gn > 0 else 0
    detail = f"Graham Number: ₹{gn:,.0f}, Price: ₹{stock.current_price:,.0f}, "
    detail += f"Margin of Safety: {margin:.0f}%"
    if passed:
        detail += " — Below 70% of Graham Value ✓"
    else:
        detail += f" — Needs price ≤ ₹{threshold:,.0f}"
    return passed, detail


def evaluate_c9(stock: Stock) -> tuple[bool, str]:
    passed = stock.pb_ratio < stock.sector_avg_pb if stock.sector_avg_pb > 0 else False
    detail = f"P/B: {stock.pb_ratio:.2f}, Sector avg P/B: {stock.sector_avg_pb:.2f}"
    if passed:
        detail += " — Below sector average ✓"
    else:
        detail += " — Above sector average"
    return passed, detail


def evaluate_d10(stock: Stock) -> tuple[bool, str]:
    high_holding = stock.promoter_holding_pct >= 50
    low_pledge = stock.promoter_pledge_pct <= 5
    passed = high_holding and low_pledge
    detail = f"Promoter holding: {stock.promoter_holding_pct:.1f}%, Pledge: {stock.promoter_pledge_pct:.1f}%"
    if passed:
        detail += " — Strong promoter commitment ✓"
    else:
        parts = []
        if not high_holding:
            parts.append("Holding < 50%")
        if not low_pledge:
            parts.append(f"Pledge {stock.promoter_pledge_pct:.1f}% (risky)")
        detail += f" — {', '.join(parts)}"
    return passed, detail


def evaluate_d11(stock: Stock) -> tuple[bool, str]:
    passed = stock.dividend_years >= 7
    detail = f"Dividends paid in {stock.dividend_years}/10 years"
    if passed:
        detail += f", Yield: {stock.dividend_yield:.2f}% — Consistent payer ✓"
    else:
        detail += " — Inconsistent dividend history"
    return passed, detail


def evaluate_d12(stock: Stock) -> tuple[bool, str]:
    flat = stock.price_flat_months >= 6
    growing = stock.profit_growing
    rising = stock.volume_rising
    passed = flat and growing and rising
    detail_parts = []
    if flat:
        detail_parts.append(f"Price flat for {stock.price_flat_months} months")
    if growing:
        detail_parts.append("profits growing")
    if rising:
        detail_parts.append("volumes rising")
    detail = ", ".join(detail_parts) if detail_parts else "No accumulation signals"
    if passed:
        detail += " — Silent accumulation pattern ✓"
    else:
        missing = []
        if not flat:
            missing.append("price not flat enough")
        if not growing:
            missing.append("profits not growing")
        if not rising:
            missing.append("volumes not rising")
        if missing:
            detail += f" — Missing: {', '.join(missing)}"
    return passed, detail


def compute_gdf_score(stock: Stock) -> dict:
    a1_pass, a1_detail = evaluate_a1(stock)
    a2_pass, a2_detail = evaluate_a2(stock)
    a3_pass, a3_detail = evaluate_a3(stock)
    b4_pass, b4_detail = evaluate_b4(stock)
    b5_pass, b5_detail = evaluate_b5(stock)
    b6_pass, b6_detail = evaluate_b6(stock)
    c7_pass, c7_detail = evaluate_c7(stock)
    c8_pass, c8_detail = evaluate_c8(stock)
    c9_pass, c9_detail = evaluate_c9(stock)
    d10_pass, d10_detail = evaluate_d10(stock)
    d11_pass, d11_detail = evaluate_d11(stock)
    d12_pass, d12_detail = evaluate_d12(stock)

    signals = [
        a1_pass, a2_pass, a3_pass,
        b4_pass, b5_pass, b6_pass,
        c7_pass, c8_pass, c9_pass,
        d10_pass, d11_pass, d12_pass,
    ]
    total = sum(signals)

    graham_number = compute_graham_number(stock.eps, stock.book_value)
    margin = ((graham_number - stock.current_price) / graham_number * 100) if graham_number > 0 else 0

    if total >= 10:
        verdict = "Strong Buy on Dips"
    elif total >= 9:
        verdict = "Quality Candidate"
    elif total >= 7:
        verdict = "Watch"
    elif total >= 5:
        verdict = "Weak — Needs Improvement"
    else:
        verdict = "Avoid"

    return {
        "stock_id": stock.id,
        "symbol": stock.symbol,
        "computed_at": datetime.now(timezone.utc),
        "a1_large_stable": a1_pass, "a1_detail": a1_detail,
        "a2_consistent_profits": a2_pass, "a2_detail": a2_detail,
        "a3_economic_moat": a3_pass, "a3_detail": a3_detail,
        "b4_low_debt": b4_pass, "b4_detail": b4_detail,
        "b5_healthy_returns": b5_pass, "b5_detail": b5_detail,
        "b6_strong_cashflows": b6_pass, "b6_detail": b6_detail,
        "c7_reasonable_pe": c7_pass, "c7_detail": c7_detail,
        "c8_graham_value": c8_pass, "c8_detail": c8_detail,
        "c9_price_to_book": c9_pass, "c9_detail": c9_detail,
        "d10_promoter_quality": d10_pass, "d10_detail": d10_detail,
        "d11_dividend_consistency": d11_pass, "d11_detail": d11_detail,
        "d12_silent_accumulation": d12_pass, "d12_detail": d12_detail,
        "total_score": total,
        "graham_number": round(graham_number, 2),
        "margin_of_safety_pct": round(margin, 2),
        "verdict": verdict,
    }


def score_all_stocks(db: Session) -> int:
    stocks = db.query(Stock).all()
    count = 0
    for stock in stocks:
        score_data = compute_gdf_score(stock)
        existing = db.query(GDFScore).filter(GDFScore.stock_id == stock.id).first()
        if existing:
            for k, v in score_data.items():
                setattr(existing, k, v)
        else:
            db.add(GDFScore(**score_data))
        count += 1
    db.commit()
    return count
