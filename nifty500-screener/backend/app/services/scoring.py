"""Multi-factor scoring engine with hard filters and weighted composite score."""

from dataclasses import dataclass, field

import numpy as np

from app.config import settings


@dataclass
class ScoringInput:
    symbol: str
    # Fundamentals
    roe: float | None = None
    roce: float | None = None
    roa: float | None = None
    gross_margin: float | None = None
    ebitda_margin: float | None = None
    net_margin: float | None = None
    revenue_growth_3y: float | None = None
    eps_growth_3y: float | None = None
    profit_cagr_3y: float | None = None
    debt_to_equity: float | None = None
    interest_coverage: float | None = None
    cfo_to_ni_ratio: float | None = None
    fcf_yield: float | None = None
    earnings_variability: float | None = None
    # Valuation
    pe_ratio: float | None = None
    pb_ratio: float | None = None
    ev_to_ebitda: float | None = None
    peg_ratio: float | None = None
    pe_zscore_5y: float | None = None
    pb_zscore_5y: float | None = None
    ev_ebitda_vs_sector: float | None = None
    earnings_yield: float | None = None
    # Technical
    rsi_14: float | None = None
    distance_from_200sma: float | None = None
    sma_200_slope: float | None = None
    macd_histogram: float | None = None
    bb_squeeze: bool | None = None
    rs_vs_nifty500: float | None = None
    ma_crossover: str | None = None
    # Patterns
    breakout_detected: bool = False
    volume_expansion: bool = False
    higher_high: bool = False
    higher_low: bool = False
    trend_template_pass: bool = False
    # Insider/News
    promoter_buying: bool = False
    promoter_pledge_pct: float | None = None
    clustered_buys: bool = False
    news_sentiment_score: float | None = None
    # Liquidity
    avg_daily_value: float | None = None


@dataclass
class ScoreResult:
    symbol: str
    total_score: float = 0.0
    fundamentals_score: float = 0.0
    valuation_score: float = 0.0
    technical_score: float = 0.0
    pattern_score: float = 0.0
    insider_news_score: float = 0.0
    passed_hard_filters: bool = False
    filter_failures: list[str] = field(default_factory=list)
    top_factors: dict = field(default_factory=dict)
    key_risks: dict = field(default_factory=dict)
    missing_data: list[str] = field(default_factory=list)
    reasons_text: str = ""
    data_completeness: float = 0.0


def _safe_score(value: float | None, ideal: float, scale: float, higher_better: bool = True) -> float:
    if value is None:
        return 0.0
    if higher_better:
        raw = min(max((value - ideal + scale) / scale, 0), 1)
    else:
        raw = min(max((ideal + scale - value) / scale, 0), 1)
    return raw * 100


def apply_hard_filters(inp: ScoringInput) -> list[str]:
    failures: list[str] = []
    if inp.avg_daily_value is not None and inp.avg_daily_value < settings.min_avg_daily_value:
        failures.append(f"Low liquidity: ₹{inp.avg_daily_value:,.0f} < ₹{settings.min_avg_daily_value:,.0f}")
    if inp.debt_to_equity is not None and inp.debt_to_equity > settings.max_debt_equity:
        failures.append(f"High leverage: D/E {inp.debt_to_equity:.1f} > {settings.max_debt_equity}")
    if inp.promoter_pledge_pct is not None and inp.promoter_pledge_pct > settings.max_promoter_pledge_pct:
        failures.append(f"High pledge: {inp.promoter_pledge_pct:.1f}% > {settings.max_promoter_pledge_pct}%")
    return failures


def score_fundamentals(inp: ScoringInput) -> tuple[float, dict, list[str]]:
    scores: dict[str, float] = {}
    missing: list[str] = []

    fields = [
        ("ROE", inp.roe, 15, 20, True),
        ("ROCE", inp.roce, 15, 20, True),
        ("Gross Margin", inp.gross_margin, 30, 40, True),
        ("EBITDA Margin", inp.ebitda_margin, 15, 20, True),
        ("Revenue Growth 3Y", inp.revenue_growth_3y, 10, 30, True),
        ("EPS Growth 3Y", inp.eps_growth_3y, 10, 30, True),
        ("D/E Ratio", inp.debt_to_equity, 1.0, 2.0, False),
        ("Interest Coverage", inp.interest_coverage, 3, 10, True),
        ("CFO/NI", inp.cfo_to_ni_ratio, 0.8, 0.5, True),
        ("FCF Yield", inp.fcf_yield, 3, 8, True),
    ]

    for name, val, ideal, scale, higher in fields:
        if val is None:
            missing.append(name)
        else:
            scores[name] = _safe_score(val, ideal, scale, higher)

    avg = np.mean(list(scores.values())) if scores else 0.0
    return float(avg), scores, missing


def score_valuation(inp: ScoringInput) -> tuple[float, dict, list[str]]:
    scores: dict[str, float] = {}
    missing: list[str] = []

    if inp.pe_zscore_5y is not None:
        scores["PE Z-Score"] = max(0, min(100, 50 - inp.pe_zscore_5y * 25))
    else:
        missing.append("PE Z-Score")

    if inp.pb_zscore_5y is not None:
        scores["PB Z-Score"] = max(0, min(100, 50 - inp.pb_zscore_5y * 25))
    else:
        missing.append("PB Z-Score")

    if inp.ev_ebitda_vs_sector is not None:
        scores["EV/EBITDA vs Sector"] = max(0, min(100, 50 - inp.ev_ebitda_vs_sector * 25))
    else:
        missing.append("EV/EBITDA vs Sector")

    if inp.peg_ratio is not None:
        scores["PEG"] = _safe_score(inp.peg_ratio, 1.0, 2.0, False)
    else:
        missing.append("PEG")

    if inp.fcf_yield is not None:
        scores["FCF Yield"] = _safe_score(inp.fcf_yield, 3, 8, True)
    elif "FCF Yield" not in missing:
        missing.append("FCF Yield")

    if inp.earnings_yield is not None:
        scores["Earnings Yield"] = _safe_score(inp.earnings_yield, 5, 10, True)
    else:
        missing.append("Earnings Yield")

    avg = np.mean(list(scores.values())) if scores else 0.0
    return float(avg), scores, missing


def score_technical(inp: ScoringInput) -> tuple[float, dict, list[str]]:
    scores: dict[str, float] = {}
    missing: list[str] = []

    if inp.rsi_14 is not None:
        if 40 <= inp.rsi_14 <= 70:
            scores["RSI"] = 80.0
        elif 30 <= inp.rsi_14 < 40 or 70 < inp.rsi_14 <= 80:
            scores["RSI"] = 50.0
        else:
            scores["RSI"] = 20.0
    else:
        missing.append("RSI")

    if inp.distance_from_200sma is not None:
        if 0 < inp.distance_from_200sma < 20:
            scores["Trend Position"] = 80.0
        elif -5 <= inp.distance_from_200sma <= 0:
            scores["Trend Position"] = 50.0
        else:
            scores["Trend Position"] = 30.0
    else:
        missing.append("Trend Position")

    if inp.sma_200_slope is not None:
        scores["MA Slope"] = min(100, max(0, 50 + inp.sma_200_slope * 10))
    else:
        missing.append("MA Slope")

    if inp.macd_histogram is not None:
        scores["MACD"] = 70.0 if inp.macd_histogram > 0 else 30.0
    else:
        missing.append("MACD")

    if inp.rs_vs_nifty500 is not None:
        scores["Relative Strength"] = min(100, max(0, inp.rs_vs_nifty500 * 50))
    else:
        missing.append("Relative Strength")

    if inp.ma_crossover == "golden_cross":
        scores["MA Crossover"] = 90.0
    elif inp.ma_crossover == "death_cross":
        scores["MA Crossover"] = 10.0
    else:
        scores["MA Crossover"] = 50.0

    avg = np.mean(list(scores.values())) if scores else 0.0
    return float(avg), scores, missing


def score_patterns(inp: ScoringInput) -> tuple[float, dict]:
    scores: dict[str, float] = {}

    scores["Breakout"] = 90.0 if inp.breakout_detected else 30.0
    scores["Volume Confirmation"] = 80.0 if inp.volume_expansion else 40.0
    scores["HH/HL Structure"] = 85.0 if (inp.higher_high and inp.higher_low) else 35.0
    scores["Trend Template"] = 90.0 if inp.trend_template_pass else 30.0

    avg = np.mean(list(scores.values())) if scores else 0.0
    return float(avg), scores


def score_insider_news(inp: ScoringInput) -> tuple[float, dict]:
    scores: dict[str, float] = {}

    if inp.promoter_buying:
        scores["Promoter Buying"] = 80.0
    else:
        scores["Promoter Buying"] = 40.0

    if inp.clustered_buys:
        scores["Clustered Buys"] = 90.0
    else:
        scores["Clustered Buys"] = 40.0

    if inp.news_sentiment_score is not None:
        scores["News Sentiment"] = min(100, max(0, 50 + inp.news_sentiment_score * 50))
    else:
        scores["News Sentiment"] = 50.0

    avg = np.mean(list(scores.values())) if scores else 0.0
    return float(avg), scores


def compute_score(inp: ScoringInput) -> ScoreResult:
    result = ScoreResult(symbol=inp.symbol)

    filter_failures = apply_hard_filters(inp)
    result.filter_failures = filter_failures
    result.passed_hard_filters = len(filter_failures) == 0

    fund_score, fund_details, fund_missing = score_fundamentals(inp)
    val_score, val_details, val_missing = score_valuation(inp)
    tech_score, tech_details, tech_missing = score_technical(inp)
    pat_score, pat_details = score_patterns(inp)
    ins_score, ins_details = score_insider_news(inp)

    result.fundamentals_score = round(fund_score, 2)
    result.valuation_score = round(val_score, 2)
    result.technical_score = round(tech_score, 2)
    result.pattern_score = round(pat_score, 2)
    result.insider_news_score = round(ins_score, 2)

    result.total_score = round(
        fund_score * settings.weight_fundamentals
        + val_score * settings.weight_valuation
        + tech_score * settings.weight_technical
        + pat_score * settings.weight_patterns
        + ins_score * settings.weight_insider_news,
        2,
    )

    all_details = {**fund_details, **val_details, **tech_details, **pat_details, **ins_details}
    sorted_factors = sorted(all_details.items(), key=lambda x: x[1], reverse=True)
    result.top_factors = dict(sorted_factors[:5])
    result.key_risks = dict(sorted_factors[-3:])

    all_missing = fund_missing + val_missing + tech_missing
    result.missing_data = all_missing

    all_fields = 10 + 6 + 6 + 4 + 3
    available = all_fields - len(all_missing)
    result.data_completeness = round(available / all_fields, 2)

    top_names = [f[0] for f in sorted_factors[:3]]
    risk_names = [f[0] for f in sorted_factors[-2:]]
    result.reasons_text = (
        f"Score {result.total_score:.0f}/100. "
        f"Strengths: {', '.join(top_names)}. "
        f"Weaknesses: {', '.join(risk_names)}."
    )

    if not result.passed_hard_filters:
        result.reasons_text += f" FILTERED: {'; '.join(filter_failures)}."

    return result
