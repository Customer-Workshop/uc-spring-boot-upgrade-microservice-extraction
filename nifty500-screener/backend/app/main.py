"""NIFTY 500 Stock Screener — FastAPI application."""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.auth import router as auth_router
from app.api.backtest import router as backtest_router
from app.api.rankings import router as rankings_router
from app.api.signals import router as signals_router
from app.api.stock import router as stock_router
from app.api.universe import router as universe_router
from app.config import settings
from app.database import Base, engine

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Creating database tables...")
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables created.")

    if not settings.alpha_vantage_api_key:
        logger.warning(
            "No API keys configured — running in MOCK DATA mode. "
            "Set ALPHA_VANTAGE_API_KEY in .env for real data."
        )

    yield
    logger.info("Shutting down...")


app = FastAPI(
    title=settings.app_name,
    description=(
        "Research-only NIFTY 500 Top-20 Stock Screener. "
        "Ranks stocks using fundamentals, valuation, technical indicators, "
        "patterns, insider/promoter signals, and news/macro regime. "
        "NOT investment advice."
    ),
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:3001", "*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(universe_router)
app.include_router(rankings_router)
app.include_router(stock_router)
app.include_router(backtest_router)
app.include_router(signals_router)
app.include_router(auth_router)


@app.get("/")
def root():
    return {
        "app": settings.app_name,
        "version": "1.0.0",
        "disclaimer": "Research tool only — NOT investment advice.",
        "docs": "/docs",
    }


@app.get("/health")
def health():
    return {"status": "healthy"}
