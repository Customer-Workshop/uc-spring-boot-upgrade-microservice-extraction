from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Kite Trading Platform"
    debug: bool = False
    database_url: str = "sqlite:///./kite.db"
    secret_key: str = "change-me-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 1440

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
