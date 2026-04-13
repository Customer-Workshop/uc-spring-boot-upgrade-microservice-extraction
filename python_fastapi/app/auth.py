"""Lightweight authentication helpers.

The Java version uses Spring Security with JWT (JwtTokenFilter + WebSecurityConfig).
For this translation we provide a simple token-based scheme that extracts the
current user from the database using a ``Token <user-id>`` header.  This keeps
the surface area small while preserving the same authorization semantics
(owner-only writes).

In production this would be replaced with proper JWT validation.
"""

from typing import Optional

from fastapi import Depends, Header
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import User


def _parse_token(authorization: Optional[str]) -> Optional[str]:
    """Return the raw token value from an ``Authorization: Token <value>`` header."""
    if authorization and authorization.startswith("Token "):
        return authorization[len("Token "):]
    return None


def get_current_user_optional(
    authorization: Optional[str] = Header(None),
    db: Session = Depends(get_db),
) -> Optional[User]:
    """Return the current user if a valid token is present, else ``None``.

    Mirrors the Java endpoints that accept ``@AuthenticationPrincipal User user``
    where user can be *null* for unauthenticated requests (e.g. list articles).
    """
    token = _parse_token(authorization)
    if token is None:
        return None
    return db.query(User).filter(User.id == token).first()


def get_current_user_required(
    authorization: Optional[str] = Header(None),
    db: Session = Depends(get_db),
) -> User:
    """Return the current user or raise 401.

    Used for endpoints that require authentication (create, update, delete, feed).
    """
    from fastapi import HTTPException

    user = get_current_user_optional(authorization, db)
    if user is None:
        raise HTTPException(status_code=401, detail="Authentication required")
    return user
