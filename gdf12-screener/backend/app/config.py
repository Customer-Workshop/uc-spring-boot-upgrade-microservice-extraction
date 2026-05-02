from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "GDF-12 Defensive Stock Screener"
    debug: bool = False
    database_url: str = "sqlite:///./gdf12.db"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
