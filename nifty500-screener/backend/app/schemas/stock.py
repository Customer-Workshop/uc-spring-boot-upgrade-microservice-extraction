from datetime import date, datetime

from pydantic import BaseModel


class StockBase(BaseModel):
    symbol: str
    company_name: str
    industry: str | None = None
    sector: str | None = None
    isin: str | None = None


class StockResponse(StockBase):
    id: int
    market_cap: float | None = None
    is_nifty500: bool = True
    avg_daily_value: float | None = None

    model_config = {"from_attributes": True}


class OHLCVResponse(BaseModel):
    date: date
    open: float
    high: float
    low: float
    close: float
    volume: float
    adjusted_close: float | None = None

    model_config = {"from_attributes": True}


class FundamentalsResponse(BaseModel):
    report_date: date
    period: str
    roe: float | None = None
    roce: float | None = None
    roa: float | None = None
    gross_margin: float | None = None
    ebitda_margin: float | None = None
    net_margin: float | None = None
    revenue: float | None = None
    eps: float | None = None
    revenue_growth_3y: float | None = None
    eps_growth_3y: float | None = None
    debt_to_equity: float | None = None
    interest_coverage: float | None = None
    pe_ratio: float | None = None
    pb_ratio: float | None = None
    ev_to_ebitda: float | None = None
    fcf_yield: float | None = None

    model_config = {"from_attributes": True}


class TechnicalSignalResponse(BaseModel):
    date: date
    rsi_14: float | None = None
    sma_20: float | None = None
    sma_50: float | None = None
    sma_200: float | None = None
    macd_line: float | None = None
    macd_signal: float | None = None
    macd_histogram: float | None = None
    bb_upper: float | None = None
    bb_middle: float | None = None
    bb_lower: float | None = None
    atr_14: float | None = None
    rs_vs_nifty500: float | None = None

    model_config = {"from_attributes": True}


class DisclosureResponse(BaseModel):
    disclosure_date: date
    disclosure_type: str
    entity_name: str | None = None
    transaction_type: str
    shares: float | None = None
    value_inr: float | None = None
    promoter_holding_pct: float | None = None
    promoter_pledge_pct: float | None = None
    signal_strength: float | None = None

    model_config = {"from_attributes": True}


class NewsItemResponse(BaseModel):
    title: str
    summary: str | None = None
    url: str | None = None
    source: str | None = None
    published_at: datetime | None = None
    sentiment: str | None = None
    sentiment_score: float | None = None
    impact_score: float | None = None
    category: str | None = None

    model_config = {"from_attributes": True}


class StockSnapshotResponse(BaseModel):
    stock: StockResponse
    latest_price: OHLCVResponse | None = None
    fundamentals: FundamentalsResponse | None = None
    technical: TechnicalSignalResponse | None = None
    disclosures: list[DisclosureResponse] = []
    news: list[NewsItemResponse] = []
    score: "ScoreResponse | None" = None


class ScoreResponse(BaseModel):
    date: date
    total_score: float
    rank: int | None = None
    fundamentals_score: float
    valuation_score: float
    technical_score: float
    pattern_score: float
    insider_news_score: float
    passed_hard_filters: bool
    filter_failures: str | None = None
    top_factors: dict | None = None
    key_risks: dict | None = None
    missing_data: dict | None = None
    reasons_text: str | None = None
    data_completeness: float

    model_config = {"from_attributes": True}


StockSnapshotResponse.model_rebuild()
