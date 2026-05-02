"""News and macro impact analysis for Indian stock market.

Provides geopolitical, employment, promoter news impact assessment
for each sector and stock.
"""

from typing import Any

# Sector-specific macro sensitivity mapping
SECTOR_MACRO_SENSITIVITY: dict[str, dict[str, Any]] = {
    "IT Services": {
        "usd_inr": "positive",  # Weak INR helps IT revenues
        "us_recession": "negative",
        "rbi_rate_hike": "neutral",
        "crude_oil": "neutral",
        "geopolitical_risk": "low",
        "employment_sensitivity": "moderate",
        "key_drivers": [
            "US tech spending cycle",
            "USD-INR exchange rate",
            "Digital transformation demand",
            "H1B visa policies",
        ],
        "current_outlook": "Cautiously Optimistic",
        "outlook_detail": (
            "US tech spending stabilizing after downturn. Weak rupee provides margin tailwind. "
            "AI/cloud demand creating new revenue streams. Wage inflation moderating."
        ),
    },
    "Banking": {
        "usd_inr": "neutral",
        "us_recession": "indirect",
        "rbi_rate_hike": "mixed",  # NIM improves but loan growth may slow
        "crude_oil": "indirect",
        "geopolitical_risk": "low",
        "employment_sensitivity": "high",
        "key_drivers": [
            "RBI monetary policy and repo rate",
            "Credit growth trajectory",
            "NPA/asset quality trends",
            "Deposit rate competition",
        ],
        "current_outlook": "Positive",
        "outlook_detail": (
            "Strong credit growth (15-16% YoY). NPA cycle at multi-year lows. "
            "Net Interest Margins stable. PSU bank reforms driving re-rating."
        ),
    },
    "FMCG": {
        "usd_inr": "slightly_negative",
        "us_recession": "neutral",
        "rbi_rate_hike": "neutral",
        "crude_oil": "negative",  # Input costs
        "geopolitical_risk": "low",
        "employment_sensitivity": "high",
        "key_drivers": [
            "Rural consumption recovery",
            "Raw material inflation (palm oil, crude derivatives)",
            "Distribution expansion",
            "Urban demand stability",
        ],
        "current_outlook": "Stable",
        "outlook_detail": (
            "Volume growth recovering as rural demand stabilizes. Input cost pressures easing. "
            "Premium product mix improving margins. Competition from D2C brands manageable."
        ),
    },
    "Pharma": {
        "usd_inr": "positive",
        "us_recession": "neutral",
        "rbi_rate_hike": "neutral",
        "crude_oil": "slightly_negative",
        "geopolitical_risk": "moderate",
        "employment_sensitivity": "low",
        "key_drivers": [
            "US FDA inspection outcomes",
            "US generics pricing pressure",
            "India domestic demand growth",
            "Complex generics/biosimilar pipeline",
        ],
        "current_outlook": "Cautiously Positive",
        "outlook_detail": (
            "US generics pricing stabilizing after years of erosion. India domestic market growing 10-12%. "
            "Complex generics offering higher barriers. CDMO/contract manufacturing growing."
        ),
    },
    "Energy": {
        "usd_inr": "negative",
        "us_recession": "negative",
        "rbi_rate_hike": "neutral",
        "crude_oil": "high_sensitivity",
        "geopolitical_risk": "high",
        "employment_sensitivity": "moderate",
        "key_drivers": [
            "Global crude oil prices",
            "Government fuel pricing policy",
            "Refining margins (GRMs)",
            "Natural gas pricing reforms",
        ],
        "current_outlook": "Mixed",
        "outlook_detail": (
            "Crude prices range-bound ($70-85). Refining margins normalizing. "
            "Government subsidy policy uncertainty persists. Green energy transition underway."
        ),
    },
    "Automobile": {
        "usd_inr": "slightly_negative",
        "us_recession": "indirect",
        "rbi_rate_hike": "negative",
        "crude_oil": "negative",
        "geopolitical_risk": "moderate",
        "employment_sensitivity": "very_high",
        "key_drivers": [
            "Consumer sentiment and employment",
            "Interest rates (auto loan EMIs)",
            "Raw material costs (steel, aluminum)",
            "EV transition pace",
        ],
        "current_outlook": "Positive",
        "outlook_detail": (
            "SUV/premium segment driving growth. Two-wheeler recovery underway. "
            "EV penetration accelerating. Export markets diversifying beyond traditional."
        ),
    },
    "Gas / Pipelines": {
        "usd_inr": "slightly_negative",
        "us_recession": "neutral",
        "rbi_rate_hike": "neutral",
        "crude_oil": "moderate_sensitivity",
        "geopolitical_risk": "moderate",
        "employment_sensitivity": "low",
        "key_drivers": [
            "Natural gas pricing (APM)",
            "Pipeline network expansion",
            "LPG subsidy policy",
            "City gas distribution growth",
        ],
        "current_outlook": "Positive",
        "outlook_detail": (
            "Gas pipeline infrastructure expanding rapidly. City gas volumes growing 15-20%. "
            "Government push for natural gas as transition fuel. Stable regulated returns."
        ),
    },
    "Utilities": {
        "usd_inr": "neutral",
        "us_recession": "neutral",
        "rbi_rate_hike": "slightly_negative",
        "crude_oil": "neutral",
        "geopolitical_risk": "low",
        "employment_sensitivity": "low",
        "key_drivers": [
            "Government power sector reforms",
            "Renewable energy capacity additions",
            "Power demand growth (6-8%)",
            "Distribution loss reduction",
        ],
        "current_outlook": "Positive",
        "outlook_detail": (
            "India's power demand growing strongly. Renewable capacity additions accelerating. "
            "Government reforms improving distribution efficiency. Stable regulated returns."
        ),
    },
}

# Global macro events and their market impact
GLOBAL_MACRO_EVENTS: list[dict[str, str]] = [
    {
        "category": "Geopolitical",
        "event": "India-China Border Tensions",
        "impact": "Moderate negative for market sentiment. Positive for defense stocks. "
                  "Limited direct impact on most sectors except auto parts/chemicals with China supply chain.",
        "severity": "medium",
    },
    {
        "category": "Geopolitical",
        "event": "Middle East Instability & Oil Routes",
        "impact": "High risk for crude oil prices. Direct impact on OMCs, airlines, chemicals. "
                  "Indirect impact via inflation on FMCG and consumer sectors.",
        "severity": "high",
    },
    {
        "category": "Geopolitical",
        "event": "US-China Trade War / Tech Restrictions",
        "impact": "Mixed for India — potential beneficiary of China+1 supply chain shift. "
                  "Positive for Indian IT and manufacturing. Risk for global demand slowdown.",
        "severity": "medium",
    },
    {
        "category": "Employment",
        "event": "India Urban Employment Trends",
        "impact": "Urban employment improving per CMIE data. Positive for consumption, "
                  "banking (retail loans), auto, and FMCG sectors.",
        "severity": "medium",
    },
    {
        "category": "Employment",
        "event": "Rural Wage Growth & MGNREGA Demand",
        "impact": "Rural wages growing slowly. MGNREGA demand stabilizing. Key for FMCG volumes, "
                  "two-wheeler demand, and agricultural input companies.",
        "severity": "medium",
    },
    {
        "category": "Monetary Policy",
        "event": "RBI Monetary Policy Stance",
        "impact": "RBI in accommodative-to-neutral mode. Rate cuts support growth stocks and banking NIMs. "
                  "Stable rates positive for NBFCs and housing finance.",
        "severity": "high",
    },
    {
        "category": "Global",
        "event": "US Federal Reserve Policy",
        "impact": "US rate trajectory affects FII flows into India. Rate cuts positive for emerging markets. "
                  "USD weakness supports IT sector margins.",
        "severity": "high",
    },
    {
        "category": "Fiscal Policy",
        "event": "India Government Capex & Infrastructure Push",
        "impact": "₹11+ lakh crore capex budget. Positive for infra, cement, steel, capital goods. "
                  "Multiplier effect on employment and consumption.",
        "severity": "high",
    },
]


def get_sector_impact(sector: str) -> dict[str, Any]:
    """Get macro impact analysis for a sector."""
    default = {
        "key_drivers": ["General economic growth", "Sector-specific regulations"],
        "current_outlook": "Neutral",
        "outlook_detail": "Monitor sector-specific developments.",
        "geopolitical_risk": "moderate",
        "employment_sensitivity": "moderate",
    }
    return SECTOR_MACRO_SENSITIVITY.get(sector, default)


def get_relevant_macro_events(sector: str) -> list[dict[str, str]]:
    """Get macro events most relevant to a sector."""
    all_events = GLOBAL_MACRO_EVENTS.copy()

    # Prioritize events by sector relevance
    sector_priorities: dict[str, list[str]] = {
        "Energy": ["Middle East", "crude", "oil"],
        "IT Services": ["US-China", "Federal Reserve", "USD"],
        "Banking": ["RBI", "Employment", "Credit"],
        "FMCG": ["Employment", "Rural", "Inflation"],
        "Pharma": ["FDA", "US-China", "Regulatory"],
        "Automobile": ["Employment", "Interest", "crude"],
        "Gas / Pipelines": ["Middle East", "crude", "Infrastructure"],
        "Utilities": ["Infrastructure", "Government", "RBI"],
    }

    keywords = sector_priorities.get(sector, [])

    def relevance_score(event: dict[str, str]) -> int:
        text = event["event"] + event["impact"]
        return sum(1 for kw in keywords if kw.lower() in text.lower())

    return sorted(all_events, key=relevance_score, reverse=True)


def get_promoter_signals(
    promoter_holding: float,
    promoter_pledge: float,
    dividend_years: int,
    dividend_yield: float,
) -> dict[str, Any]:
    """Analyze promoter signals and generate insights."""
    signals: list[str] = []
    risk_level = "low"

    if promoter_holding >= 60:
        signals.append(f"High promoter stake ({promoter_holding:.1f}%) — strong insider confidence and alignment")
    elif promoter_holding >= 50:
        signals.append(f"Adequate promoter holding ({promoter_holding:.1f}%) — decent skin in the game")
    elif promoter_holding >= 30:
        signals.append(f"Moderate promoter stake ({promoter_holding:.1f}%) — monitor for any further dilution")
        risk_level = "medium"
    else:
        signals.append(f"Low promoter holding ({promoter_holding:.1f}%) — may indicate dispersed ownership or MNC subsidiary")
        risk_level = "medium"

    if promoter_pledge > 20:
        signals.append(f"WARNING: {promoter_pledge:.1f}% shares pledged — margin call risk during sharp market declines")
        risk_level = "high"
    elif promoter_pledge > 5:
        signals.append(f"Moderate pledge ({promoter_pledge:.1f}%) — monitor for reduction in coming quarters")
        risk_level = "medium"
    elif promoter_pledge > 0:
        signals.append(f"Minimal pledge ({promoter_pledge:.1f}%) — not a concern")
    else:
        signals.append("No shares pledged — clean governance signal")

    if dividend_years >= 10:
        signals.append(f"Strong dividend track record ({dividend_years} years) — shareholder-friendly management")
    elif dividend_years >= 7:
        signals.append(f"Good dividend history ({dividend_years} years) — consistent cash return policy")

    if dividend_yield >= 3:
        signals.append(f"Attractive dividend yield ({dividend_yield:.1f}%) — provides income while holding")
    elif dividend_yield >= 1.5:
        signals.append(f"Decent dividend yield ({dividend_yield:.1f}%) — supplementary income")

    return {
        "signals": signals,
        "risk_level": risk_level,
        "promoter_confidence": "high" if promoter_holding >= 55 and promoter_pledge < 5 else
                               "medium" if promoter_holding >= 40 else "low",
    }
