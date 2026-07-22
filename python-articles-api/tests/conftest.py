from __future__ import annotations

import json
import os
import re
import tempfile
from pathlib import Path

import jwt
import pytest

GOLDEN_DIR = Path(__file__).parent / "golden"
_TS_RE = re.compile(r"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z")


@pytest.fixture(scope="session")
def client():
    # Fresh temp database seeded on startup, isolated from dev.db.
    tmp = tempfile.NamedTemporaryFile(suffix=".db", delete=False)
    tmp.close()
    os.environ["DATABASE_URL"] = f"sqlite:///{tmp.name}"

    from fastapi.testclient import TestClient

    from app.main import app

    with TestClient(app) as c:
        yield c
    os.unlink(tmp.name)


def _token(user_id: str) -> str:
    from app.config import JWT_ALGORITHM, JWT_SECRET

    return jwt.encode({"sub": user_id}, JWT_SECRET, algorithm=JWT_ALGORITHM)


@pytest.fixture
def john_headers():
    return {"Authorization": f"Token {_token('user-1')}"}


@pytest.fixture
def jane_headers():
    return {"Authorization": f"Token {_token('user-2')}"}


def load_golden(name: str):
    with open(GOLDEN_DIR / name) as f:
        return json.load(f)


def normalize(value):
    """Replace time-relative timestamp strings so Java/Python responses can be
    compared for structural + value parity independent of when each was seeded."""
    if isinstance(value, dict):
        return {k: normalize(v) for k, v in value.items()}
    if isinstance(value, list):
        return [normalize(v) for v in value]
    if isinstance(value, str) and _TS_RE.fullmatch(value):
        return "<TS>"
    return value
