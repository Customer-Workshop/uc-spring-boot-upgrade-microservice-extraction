from fastapi import FastAPI

from app.database import Base, engine
from app.routers.articles import router as articles_router

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Conduit Articles API", version="0.1.0")
app.include_router(articles_router)
