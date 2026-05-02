from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from app.api.ai import router as ai_router
from app.api.etf import router as etf_router
from app.api.screener import router as screener_router
from app.api.swing import router as swing_router
from app.api.watchlist import router as watchlist_router
from app.database import Base, engine, get_db
from app.services.etf_engine import score_all_etfs
from app.services.etf_seeder import seed_etfs
from app.services.gdf_engine import score_all_stocks
from app.services.seeder import seed_stocks
from app.services.swing_engine import score_all_swing_stocks
from app.services.swing_seeder import seed_swing_stocks


@asynccontextmanager
async def lifespan(application: FastAPI):
    Base.metadata.create_all(bind=engine)
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


@app.get("/api/health")
def health():
    return {"status": "ok", "app": "Stock & ETF Explorer"}
