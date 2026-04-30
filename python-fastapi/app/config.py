from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "sqlite:///./conduit.db"
    jwt_secret: str = (
        "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA"
    )
    jwt_algorithm: str = "HS512"
    jwt_session_time: int = 86400
    default_image: str = "https://static.productionready.io/images/smiley-cyrus.jpg"

    model_config = {"env_prefix": "CONDUIT_"}


settings = Settings()
