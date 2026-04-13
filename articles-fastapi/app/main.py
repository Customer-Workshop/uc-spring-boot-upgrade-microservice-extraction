from fastapi import FastAPI
from .database import init_db
from .router import router

app = FastAPI(title="Articles API", version="1.0.0")

app.include_router(router)


@app.on_event("startup")
def startup():
    init_db()


@app.get("/tags")
def get_tags():
    from .database import get_db, Tag

    db = next(get_db())
    tags = db.query(Tag).all()
    return {"tags": [t.name for t in tags]}
