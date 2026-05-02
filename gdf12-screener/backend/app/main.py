import logging
import threading
from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from app.api.ai import router as ai_router
from app.api.etf import router as etf_router
from app.api.screener import router as screener_router
from app.api.swing import router as swing_router
from app.api.watchlist import router as watchlist_router
from app.database import Base, SessionLocal, engine, get_db
from app.models.stock import Stock
from app.services.etf_engine import score_all_etfs
from app.services.etf_seeder import seed_etfs
from app.services.gdf_engine import score_all_stocks
from app.services.seeder import seed_stocks
from app.services.live_refresh import (
    get_last_refresh,
    refresh_etfs,
    refresh_fundamental_stocks,
    refresh_swing_stocks,
)
from app.services.swing_engine import score_all_swing_stocks
from app.services.swing_seeder import seed_swing_stocks

logger = logging.getLogger(__name__)


def _auto_seed_and_refresh():
    """Background task: seed data if DB is empty, then refresh with live prices."""
    db = SessionLocal()
    try:
        existing = db.query(Stock).count()
        if existing == 0:
            logger.info("Auto-seeding: no stocks found in DB, seeding now...")
            seed_stocks(db)
            score_all_stocks(db)
            seed_swing_stocks(db)
            score_all_swing_stocks(db)
            seed_etfs(db)
            score_all_etfs(db)
            logger.info("Auto-seed complete. Now refreshing with live prices...")

        # Refresh with live data
        result = refresh_fundamental_stocks(db)
        if result.get("updated", 0) > 0:
            score_all_stocks(db)
            logger.info(f"Fundamentals refreshed: {result['updated']}/{result['total']}")
        result = refresh_swing_stocks(db)
        if result.get("updated", 0) > 0:
            score_all_swing_stocks(db)
            logger.info(f"Swing stocks refreshed: {result['updated']}/{result['total']}")
        result = refresh_etfs(db)
        if result.get("updated", 0) > 0:
            score_all_etfs(db)
            logger.info(f"ETFs refreshed: {result['updated']}/{result['total']}")
        logger.info("Live data refresh complete.")
    except Exception as e:
        logger.error(f"Auto-seed/refresh failed: {e}")
    finally:
        db.close()


@asynccontextmanager
async def lifespan(application: FastAPI):
    Base.metadata.create_all(bind=engine)

    # Auto-seed if DB is empty and refresh in background
    db = SessionLocal()
    try:
        existing = db.query(Stock).count()
        if existing == 0:
            logger.info("DB is empty — seeding with baseline data...")
            seed_stocks(db)
            score_all_stocks(db)
            seed_swing_stocks(db)
            score_all_swing_stocks(db)
            seed_etfs(db)
            score_all_etfs(db)
            logger.info("Baseline data seeded. Starting background live refresh...")
    finally:
        db.close()

    # Start live refresh in background thread (non-blocking)
    thread = threading.Thread(target=_auto_seed_and_refresh, daemon=True)
    thread.start()

    yield


app = FastAPI(
    title="Stock & ETF Explorer",
    description="3-tab screener: Fundamentals (GDF-12), Technical Swing (Confirmation Ladder), Best ETFs",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(screener_router)
app.include_router(watchlist_router)
app.include_router(swing_router)
app.include_router(etf_router)
app.include_router(ai_router)


@app.post("/api/seed")
def seed_data(db: Session = Depends(get_db)):
    fundamentals = seed_stocks(db)
    fundamental_scored = score_all_stocks(db)
    swing = seed_swing_stocks(db)
    swing_scored = score_all_swing_stocks(db)
    etfs = seed_etfs(db)
    etf_scored = score_all_etfs(db)
    return {
        "status": "seeded",
        "fundamentals": {"stocks": fundamentals, "scored": fundamental_scored},
        "swing": {"stocks": swing, "scored": swing_scored},
        "etfs": {"etfs": etfs, "scored": etf_scored},
    }


@app.post("/api/seed-and-refresh")
def seed_and_refresh(db: Session = Depends(get_db)):
    """Seed mock data then immediately refresh with live Yahoo Finance prices."""
    fundamentals = seed_stocks(db)
    score_all_stocks(db)
    swing = seed_swing_stocks(db)
    score_all_swing_stocks(db)
    etfs = seed_etfs(db)
    score_all_etfs(db)

    live_fund = refresh_fundamental_stocks(db)
    if live_fund.get("updated", 0) > 0:
        score_all_stocks(db)
    live_swing = refresh_swing_stocks(db)
    if live_swing.get("updated", 0) > 0:
        score_all_swing_stocks(db)
    live_etf = refresh_etfs(db)
    if live_etf.get("updated", 0) > 0:
        score_all_etfs(db)

    return {
        "status": "seeded_and_refreshed",
        "fundamentals": {"stocks": fundamentals, "live_updated": live_fund.get("updated", 0)},
        "swing": {"stocks": swing, "live_updated": live_swing.get("updated", 0)},
        "etfs": {"etfs": etfs, "live_updated": live_etf.get("updated", 0)},
    }


@app.post("/api/refresh")
def refresh_live_data(db: Session = Depends(get_db)):
    """Refresh all data with live prices from Yahoo Finance, then re-score."""
    # Auto-seed if DB is empty
    if db.query(Stock).count() == 0:
        seed_stocks(db)
        score_all_stocks(db)
        seed_swing_stocks(db)
        score_all_swing_stocks(db)
        seed_etfs(db)
        score_all_etfs(db)

    fundamentals = refresh_fundamental_stocks(db)
    fundamental_scored = score_all_stocks(db) if fundamentals.get("updated", 0) > 0 else 0
    swing = refresh_swing_stocks(db)
    swing_scored = score_all_swing_stocks(db) if swing.get("updated", 0) > 0 else 0
    etf = refresh_etfs(db)
    etf_scored = score_all_etfs(db) if etf.get("updated", 0) > 0 else 0
    return {
        "status": "refreshed",
        "fundamentals": {**fundamentals, "rescored": fundamental_scored},
        "swing": {**swing, "rescored": swing_scored},
        "etfs": {**etf, "rescored": etf_scored},
    }


@app.post("/api/refresh/fundamentals")
def refresh_fundamentals(db: Session = Depends(get_db)):
    """Refresh fundamental stocks only with live prices."""
    result = refresh_fundamental_stocks(db)
    scored = score_all_stocks(db) if result.get("updated", 0) > 0 else 0
    return {**result, "rescored": scored}


@app.post("/api/refresh/swing")
def refresh_swing(db: Session = Depends(get_db)):
    """Refresh swing stocks only with live technicals."""
    result = refresh_swing_stocks(db)
    scored = score_all_swing_stocks(db) if result.get("updated", 0) > 0 else 0
    return {**result, "rescored": scored}


@app.post("/api/refresh/etfs")
def refresh_etf_data(db: Session = Depends(get_db)):
    """Refresh ETFs only with live prices."""
    result = refresh_etfs(db)
    scored = score_all_etfs(db) if result.get("updated", 0) > 0 else 0
    return {**result, "rescored": scored}


@app.get("/api/last-refresh")
def last_refresh():
    """Get timestamps of last data refresh."""
    return get_last_refresh()


@app.get("/api/health")
def health():
    return {"status": "ok", "app": "Stock & ETF Explorer"}
