"""Integration tests for API endpoints using TestClient."""

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from app.database import Base, get_db
from app.main import app

SQLALCHEMY_TEST_URL = "sqlite:///./test.db"
engine = create_engine(SQLALCHEMY_TEST_URL, connect_args={"check_same_thread": False})
TestSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


def override_get_db():
    db = TestSessionLocal()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db


@pytest.fixture(autouse=True)
def setup_db():
    Base.metadata.create_all(bind=engine)
    yield
    Base.metadata.drop_all(bind=engine)


client = TestClient(app)


def test_root():
    response = client.get("/")
    assert response.status_code == 200
    data = response.json()
    assert "app" in data
    assert "disclaimer" in data


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"


def test_universe_empty():
    response = client.get("/universe")
    assert response.status_code == 200
    assert response.json() == []


def test_seed_universe():
    response = client.post("/universe/seed")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "ok"
    assert data["count"] > 0


def test_universe_after_seed():
    client.post("/universe/seed")
    response = client.get("/universe")
    assert response.status_code == 200
    stocks = response.json()
    assert len(stocks) > 0
    assert "symbol" in stocks[0]
    assert "company_name" in stocks[0]


def test_universe_search():
    client.post("/universe/seed")
    response = client.get("/universe?search=reliance")
    assert response.status_code == 200
    stocks = response.json()
    assert any("RELIANCE" in s["symbol"] for s in stocks)


def test_universe_sectors():
    client.post("/universe/seed")
    response = client.get("/universe/sectors")
    assert response.status_code == 200
    sectors = response.json()
    assert len(sectors) > 0


def test_stock_snapshot_not_found():
    response = client.get("/stock/NONEXIST/snapshot")
    assert response.status_code == 404


def test_stock_snapshot():
    client.post("/universe/seed")
    response = client.get("/stock/RELIANCE/snapshot")
    assert response.status_code == 200
    data = response.json()
    assert data["stock"]["symbol"] == "RELIANCE"


def test_rankings_today_empty():
    response = client.get("/rankings/today")
    assert response.status_code == 200
    data = response.json()
    assert data["rankings"] == []


def test_auth_register():
    response = client.post(
        "/auth/register", json={"email": "test@example.com", "password": "testpass123"}
    )
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data


def test_auth_login():
    client.post("/auth/register", json={"email": "login@example.com", "password": "pass123"})
    response = client.post(
        "/auth/login", json={"email": "login@example.com", "password": "pass123"}
    )
    assert response.status_code == 200
    data = response.json()
    assert "access_token" in data


def test_auth_invalid_login():
    response = client.post(
        "/auth/login", json={"email": "nobody@example.com", "password": "wrong"}
    )
    assert response.status_code == 401
