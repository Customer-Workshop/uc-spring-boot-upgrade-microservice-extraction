from __future__ import annotations

from typing import Optional

import jwt
from fastapi import Depends, Request
from sqlalchemy.orm import Session

from .config import JWT_ALGORITHM, JWT_SECRET
from .database import get_db
from .models import User


def _extract_token(header: Optional[str]) -> Optional[str]:
    # Mirrors JwtTokenFilter.getTokenString: split on space, take the 2nd part.
    if not header:
        return None
    parts = header.split(" ")
    if len(parts) < 2:
        return None
    return parts[1]


def _sub_from_token(token: str) -> Optional[str]:
    try:
        claims = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        return claims.get("sub")
    except Exception:
        return None


def get_current_user_optional(
    request: Request, db: Session = Depends(get_db)
) -> Optional[User]:
    token = _extract_token(request.headers.get("Authorization"))
    if not token:
        return None
    sub = _sub_from_token(token)
    if not sub:
        return None
    return db.get(User, sub)
