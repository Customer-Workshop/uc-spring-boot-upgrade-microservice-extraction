"""FastAPI application entry-point.

Mirrors ``io.spring.RealWorldApplication`` — the Spring Boot main class.
"""

from fastapi import FastAPI

from app.database import Base, engine
from app.routes import router

# Create all tables on startup (equivalent to Spring auto-DDL)
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="RealWorld Articles API",
    description="Python/FastAPI translation of the Java/Spring Boot Articles API",
    version="1.0.0",
)

app.include_router(router)
