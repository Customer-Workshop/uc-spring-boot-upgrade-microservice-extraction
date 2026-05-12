"""FastAPI app entry point with lifespan init_db."""

import json
import os
import sys
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates
from starlette.middleware.sessions import SessionMiddleware

# Add project root to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from db.db_utils import init_db


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Initialize database on startup."""
    init_db()
    # Initialize scheduler
    try:
        from app.scheduler import start_scheduler
        start_scheduler()
    except Exception:
        pass
    yield


app = FastAPI(title="Playwright Test Automation Platform", version="2.0.0", lifespan=lifespan)

# Session middleware for auth
app.add_middleware(SessionMiddleware, secret_key=os.environ.get("SESSION_SECRET", "playwright-agent-secret-key-change-in-production"))

# Static files
static_dir = os.path.join(os.path.dirname(__file__), "static")
app.mount("/static", StaticFiles(directory=static_dir), name="static")

# Templates
templates_dir = os.path.join(os.path.dirname(__file__), "templates")
templates = Jinja2Templates(directory=templates_dir)
templates.env.filters["tojson"] = lambda v: json.dumps(v)

# Register routes
from app.routes import router
app.include_router(router)
