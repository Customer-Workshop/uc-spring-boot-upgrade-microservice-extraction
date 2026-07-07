import os

# Mirrors the Spring Boot application.properties values so JWTs issued by either
# service are mutually verifiable (HS512, subject == user id).
JWT_SECRET = os.getenv(
    "JWT_SECRET",
    "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA",
)
JWT_ALGORITHM = "HS512"
JWT_SESSION_TIME = int(os.getenv("JWT_SESSION_TIME", "86400"))

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./dev.db")

DEFAULT_IMAGE = os.getenv(
    "DEFAULT_IMAGE", "https://static.productionready.io/images/smiley-cyrus.jpg"
)
