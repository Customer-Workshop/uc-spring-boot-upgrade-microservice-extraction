from fastapi import FastAPI

from app.articles import router as articles_router
from app.database import Base, engine

Base.metadata.create_all(bind=engine)

app = FastAPI(title="RealWorld Articles API (Python/FastAPI)")
app.include_router(articles_router)
