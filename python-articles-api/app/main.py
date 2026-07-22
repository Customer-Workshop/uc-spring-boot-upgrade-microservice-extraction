from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from . import articles
from .errors import (
    InvalidRequestException,
    NoAuthorizationException,
    ResourceNotFoundException,
    UnauthorizedException,
)
from .seed import init_and_seed


@asynccontextmanager
async def lifespan(_: FastAPI):
    init_and_seed()
    yield


app = FastAPI(title="Articles API (FastAPI port)", lifespan=lifespan)


def _detail_handler(_: Request, exc) -> JSONResponse:
    return JSONResponse(status_code=exc.status_code, content=exc.detail)


for _exc in (
    InvalidRequestException,
    ResourceNotFoundException,
    NoAuthorizationException,
    UnauthorizedException,
):
    app.add_exception_handler(_exc, _detail_handler)


app.include_router(articles.router)
