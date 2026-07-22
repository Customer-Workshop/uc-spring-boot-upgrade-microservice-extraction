from __future__ import annotations

from typing import Dict, List

from fastapi import HTTPException


class InvalidRequestException(HTTPException):
    """Mirrors the 422 response produced by CustomizeExceptionHandler."""

    def __init__(self, errors: Dict[str, List[str]]):
        super().__init__(status_code=422, detail={"errors": errors})


class ResourceNotFoundException(HTTPException):
    def __init__(self):
        super().__init__(status_code=404, detail={"status": 404, "error": "Not Found"})


class NoAuthorizationException(HTTPException):
    def __init__(self):
        super().__init__(status_code=403, detail={"status": 403, "error": "Forbidden"})


class UnauthorizedException(HTTPException):
    def __init__(self):
        super().__init__(
            status_code=401, detail={"status": 401, "error": "Unauthorized"}
        )
