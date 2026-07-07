from datetime import datetime, timezone

from fastapi import Request
from fastapi.responses import JSONResponse


class ApiError(Exception):
    def __init__(self, status_code: int, reason: str):
        self.status_code = status_code
        self.reason = reason


class ResourceNotFound(ApiError):
    def __init__(self):
        super().__init__(404, "Not Found")


class NoAuthorization(ApiError):
    def __init__(self):
        super().__init__(403, "Forbidden")


class Unauthorized(ApiError):
    def __init__(self):
        super().__init__(401, "Unauthorized")


class InvalidRequest(Exception):
    """Carries RealWorld-style validation errors: {field: [messages]}."""

    def __init__(self, errors: dict):
        self.errors = errors


def _spring_error_body(status: int, reason: str, path: str) -> dict:
    # Matches Spring Boot's DefaultErrorAttributes envelope.
    return {
        "timestamp": datetime.now(timezone.utc)
        .strftime("%Y-%m-%dT%H:%M:%S.")
        + f"{datetime.now(timezone.utc).microsecond // 1000:03d}+00:00",
        "status": status,
        "error": reason,
        "path": path,
    }


async def api_error_handler(request: Request, exc: ApiError):
    if exc.status_code == 401:
        # Spring returns an empty body via HttpStatusEntryPoint.
        return JSONResponse(status_code=401, content=None)
    return JSONResponse(
        status_code=exc.status_code,
        content=_spring_error_body(exc.status_code, exc.reason, request.url.path),
    )


async def invalid_request_handler(request: Request, exc: InvalidRequest):
    return JSONResponse(status_code=422, content={"errors": exc.errors})
