from datetime import date

from pydantic import BaseModel


class RankedStock(BaseModel):
    rank: int
    symbol: str
    company_name: str
    sector: str | None = None
    total_score: float
    fundamentals_score: float
    valuation_score: float
    technical_score: float
    pattern_score: float
    insider_news_score: float
    reasons_text: str | None = None
    top_factors: dict | None = None
    key_risks: dict | None = None

    model_config = {"from_attributes": True}


class RankingsResponse(BaseModel):
    date: date
    total_universe: int
    passed_filters: int
    rankings: list[RankedStock]
