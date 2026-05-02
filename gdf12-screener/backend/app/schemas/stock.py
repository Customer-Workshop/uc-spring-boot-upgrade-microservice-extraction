from pydantic import BaseModel


class SignalDetail(BaseModel):
    passed: bool
    label: str
    detail: str


class GDFScoreResponse(BaseModel):
    symbol: str
    total_score: int
    graham_number: float
    margin_of_safety_pct: float
    verdict: str

    group_a: list[SignalDetail]
    group_b: list[SignalDetail]
    group_c: list[SignalDetail]
    group_d: list[SignalDetail]


class StockSummary(BaseModel):
    symbol: str
    name: str
    sector: str
    current_price: float
    market_cap_cr: float
    pe_ratio: float
    roe: float
    roce: float
    debt_to_equity: float
    promoter_holding_pct: float
    dividend_yield: float
    is_defensive_sector: bool
    total_score: int
    verdict: str
    graham_number: float
    margin_of_safety_pct: float


class StockDetail(BaseModel):
    symbol: str
    name: str
    sector: str
    industry: str | None
    exchange: str
    current_price: float
    high_52w: float
    low_52w: float
    market_cap_cr: float
    sales_cr: float
    net_profit_cr: float
    pe_ratio: float
    pe_5y_avg: float
    pb_ratio: float
    sector_avg_pb: float
    eps: float
    book_value: float
    roe: float
    roce: float
    debt_to_equity: float
    interest_coverage: float
    ocf_cr: float
    promoter_holding_pct: float
    promoter_pledge_pct: float
    dividend_yield: float
    dividend_years: int
    profit_years_positive: int
    ocf_positive_years: int
    is_defensive_sector: bool
    moat_description: str | None
    has_strong_brand: bool
    has_cost_advantage: bool
    has_regulatory_barrier: bool
    has_dominant_share: bool

    gdf_score: GDFScoreResponse | None


class WatchlistItemResponse(BaseModel):
    id: int
    symbol: str
    added_at: str
    notes: str | None


class WatchlistAddRequest(BaseModel):
    symbol: str
    notes: str | None = None
