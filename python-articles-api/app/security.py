import time
from typing import Optional

import jwt
from fastapi import Depends, Request
from sqlalchemy.orm import Session

from . import config
from .database import get_db
from .models import User


def generate_token(user_id: str) -> str:
    """Mirror of DefaultJwtService#toToken (HS512, subject == user id)."""
    payload = {"sub": user_id, "exp": int(time.time()) + config.JWT_SESSION_TIME}
    return jwt.encode(payload, config.JWT_SECRET, algorithm=config.JWT_ALGORITHM)


def _sub_from_token(token: str) -> Optional[str]:
    try:
        claims = jwt.decode(token, config.JWT_SECRET, algorithms=[config.JWT_ALGORITHM])
        return claims.get("sub")
    except Exception:
        return None


def _token_from_header(header: Optional[str]) -> Optional[str]:
    # Matches JwtTokenFilter#getTokenString: split on space, take the 2nd part.
    if header is None:
        return None
    parts = header.split(" ")
    if len(parts) < 2:
        return None
    return parts[1]


def optional_current_user(
    request: Request, db: Session = Depends(get_db)
) -> Optional[User]:
    token = _token_from_header(request.headers.get("Authorization"))
    if token is None:
        return None
    user_id = _sub_from_token(token)
    if user_id is None:
        return None
    return db.query(User).filter(User.id == user_id).first()
