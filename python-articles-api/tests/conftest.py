import os
import shutil
import sys
from pathlib import Path

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT))

FIXTURES = Path(__file__).parent / "fixtures"
SEED_DB = FIXTURES / "seed.db"
GOLDEN = FIXTURES / "golden"

# Usernames -> ids in the seed data.
USER_IDS = {"john": "user-1", "jane": "user-2", "bob": "user-3"}


@pytest.fixture()
def db_path(tmp_path):
    dest = tmp_path / "test.db"
    shutil.copy(SEED_DB, dest)
    return dest


@pytest.fixture()
def client(db_path):
    from app.database import get_db
    from app.main import app

    engine = create_engine(
        f"sqlite:///{db_path}", connect_args={"check_same_thread": False}
    )
    TestingSession = sessionmaker(autocommit=False, autoflush=False, bind=engine)

    def override_get_db():
        db = TestingSession()
        try:
            yield db
        finally:
            db.close()

    app.dependency_overrides[get_db] = override_get_db
    with TestClient(app) as c:
        yield c
    app.dependency_overrides.clear()
    engine.dispose()


@pytest.fixture()
def token():
    from app.security import generate_token

    def _make(username: str):
        return generate_token(USER_IDS[username])

    return _make


def auth_header(token_factory, username):
    return {"Authorization": f"Token {token_factory(username)}"}
