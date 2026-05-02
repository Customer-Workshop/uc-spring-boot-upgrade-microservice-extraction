from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Stock & ETF Explorer"
    debug: bool = False
    database_url: str = "sqlite:///./gdf12.db"
    anthropic_api_key: str = ""
    ollama_url: str = "http://localhost:11434"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
