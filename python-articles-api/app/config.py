import os

# Mirrors src/main/resources/application.properties of the Java monolith.
JWT_SECRET = os.getenv(
    "JWT_SECRET",
    "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA",
)
# Java uses io.jsonwebtoken with SignatureAlgorithm.HS512.
JWT_ALGORITHM = "HS512"
JWT_SESSION_TIME = int(os.getenv("JWT_SESSION_TIME", "86400"))

DEFAULT_IMAGE = os.getenv(
    "DEFAULT_IMAGE", "https://static.productionready.io/images/smiley-cyrus.jpg"
)

DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./dev.db")
