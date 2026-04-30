"""Kite Trading Platform — FastAPI application."""

import logging
from contextlib import asynccontextmanager

from fastapi import APIRouter, Depends, FastAPI
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session

from app.api.dashboard import router as dashboard_router
from app.api.instruments import router as instruments_router
from app.api.orders import router as orders_router
from app.api.portfolio import router as portfolio_router
from app.api.watchlists import router as watchlists_router
from app.config import settings
from app.database import Base, engine, get_db
from app.services.seed import seed_all

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Creating database tables...")
    Base.metadata.create_all(bind=engine)
    logger.info("Database tables created.")
    yield
    logger.info("Shutting down...")


app = FastAPI(
    title=settings.app_name,
    description="Zerodha Kite-style trading platform clone with mock data.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:3002"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(dashboard_router)
app.include_router(instruments_router)
app.include_router(orders_router)
app.include_router(portfolio_router)
app.include_router(watchlists_router)

seed_router = APIRouter(tags=["admin"])


@seed_router.post("/api/seed")
def seed_data(db: Session = Depends(get_db)):
    return seed_all(db)


app.include_router(seed_router)


@app.get("/")
def root():
    return {"app": settings.app_name, "version": "1.0.0"}


@app.get("/health")
def health():
    return {"status": "healthy"}
