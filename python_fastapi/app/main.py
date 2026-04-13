"""FastAPI application entry-point.

Mirrors ``io.spring.RealWorldApplication`` — the Spring Boot main class.
"""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from app.database import Base, engine
from app.exceptions import ValidationError
from app.routes import router

# Create all tables on startup (equivalent to Spring auto-DDL)
Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="RealWorld Articles API",
    description="Python/FastAPI translation of the Java/Spring Boot Articles API",
    version="1.0.0",
)


@app.exception_handler(ValidationError)
async def validation_error_handler(request: Request, exc: ValidationError):
    """Return validation errors in the same shape as the Java API.

    Java returns: {"errors": {"body": ["can't be empty"]}}
    Without this handler, FastAPI would wrap it: {"detail": {"errors": ...}}
    """
    return JSONResponse(
        status_code=exc.status_code,
        content={"errors": exc.errors},
    )


app.include_router(router)
