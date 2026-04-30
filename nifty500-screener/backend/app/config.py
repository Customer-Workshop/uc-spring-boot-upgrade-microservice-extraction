from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "NIFTY500 Stock Screener"
    debug: bool = False

    # Database
    database_url: str = "postgresql://screener:screener@localhost:5432/nifty500"

    # Redis
    redis_url: str = "redis://localhost:6379/0"

    # Celery
    celery_broker_url: str = "redis://localhost:6379/1"
    celery_result_backend: str = "redis://localhost:6379/2"

    # JWT
    secret_key: str = "change-me-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440

    # API Keys (optional — mock data used when absent)
    alpha_vantage_api_key: str = ""
    indian_api_key: str = ""

    # Scoring weights
    weight_fundamentals: float = 0.35
    weight_valuation: float = 0.20
    weight_technical: float = 0.25
    weight_patterns: float = 0.10
    weight_insider_news: float = 0.10

    # Hard filters
    min_avg_daily_value: float = 10_000_000  # INR 1 crore
    max_debt_equity: float = 3.0
    max_promoter_pledge_pct: float = 50.0
    min_data_completeness: float = 0.6

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
