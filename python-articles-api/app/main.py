from fastapi import FastAPI

from .database import Base, engine
from .errors import (
    ApiError,
    InvalidRequest,
    api_error_handler,
    invalid_request_handler,
)
from .routers import articles

app = FastAPI(title="Articles API (FastAPI port)")

# Ensure tables exist when pointed at a fresh database. When pointed at a copy of
# the Spring service's SQLite DB, the tables (and data) already exist.
Base.metadata.create_all(bind=engine)

app.add_exception_handler(ApiError, api_error_handler)
app.add_exception_handler(InvalidRequest, invalid_request_handler)

app.include_router(articles.router)


@app.get("/health")
def health():
    return {"status": "ok"}
