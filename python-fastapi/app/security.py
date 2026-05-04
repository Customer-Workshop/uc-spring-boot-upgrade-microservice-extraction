from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import Depends, HTTPException, Request
from jose import JWTError, jwt
from sqlalchemy.orm import Session

from app.database import get_db
from app.models import User

JWT_SECRET = "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA"
JWT_ALGORITHM = "HS512"
JWT_SESSION_TIME = 86400


def create_token(user: User) -> str:
    expire = datetime.now(timezone.utc) + timedelta(seconds=JWT_SESSION_TIME)
    payload = {"sub": user.id, "exp": expire}
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def _extract_token(request: Request) -> Optional[str]:
    auth = request.headers.get("Authorization")
    if auth and auth.startswith("Token "):
        return auth[6:]
    return None


def get_current_user(
    request: Request,
    db: Session = Depends(get_db),
) -> User:
    token = _extract_token(request)
    if token is None:
        raise HTTPException(status_code=401, detail="Missing authentication token")
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user_id: str = payload.get("sub", "")
    except JWTError:
        raise HTTPException(status_code=401, detail="Invalid token")
    user = db.query(User).filter(User.id == user_id).first()
    if user is None:
        raise HTTPException(status_code=401, detail="User not found")
    return user


def get_optional_user(
    request: Request,
    db: Session = Depends(get_db),
) -> Optional[User]:
    token = _extract_token(request)
    if token is None:
        return None
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user_id: str = payload.get("sub", "")
    except JWTError:
        return None
    return db.query(User).filter(User.id == user_id).first()
