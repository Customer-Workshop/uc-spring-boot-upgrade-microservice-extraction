"""FastAPI application entry-point — Python translation of the Articles API."""

from fastapi import FastAPI

from app.database import Base, engine
from app.routes import router as articles_router

# Create all tables on startup (mirrors the Java auto-DDL via MyBatis schema)
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="RealWorld Articles API (Python/FastAPI)",
    description="Drop-in replacement for the Java/Spring Boot Articles API.",
    version="1.0.0",
)

app.include_router(articles_router)
