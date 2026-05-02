import math

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine, event
from sqlalchemy.orm import sessionmaker

from app.database import Base, get_db
from app.main import app
from app.services.gdf_engine import compute_graham_number

TEST_DB_URL = "sqlite:///./test_gdf12.db"
test_engine = create_engine(TEST_DB_URL, connect_args={"check_same_thread": False})


@event.listens_for(test_engine, "connect")
def _set_sqlite_pragma(dbapi_conn, connection_record):
    cursor = dbapi_conn.cursor()
    cursor.execute("PRAGMA journal_mode=WAL")
    cursor.execute("PRAGMA foreign_keys=ON")
    cursor.close()


TestSession = sessionmaker(autocommit=False, autoflush=False, bind=test_engine)


def override_get_db():
    db = TestSession()
    try:
        yield db
    finally:
        db.close()


app.dependency_overrides[get_db] = override_get_db


@pytest.fixture(autouse=True)
def setup_db():
    Base.metadata.drop_all(bind=test_engine)
    Base.metadata.create_all(bind=test_engine)
    yield
    Base.metadata.drop_all(bind=test_engine)


client = TestClient(app)


class TestHealth:
    def test_health(self):
        resp = client.get("/api/health")
        assert resp.status_code == 200
        assert resp.json()["status"] == "ok"


class TestSeeding:
    def test_seed_data(self):
        resp = client.post("/api/seed")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "seeded"
        assert data["fundamentals"]["stocks"] > 0
        assert data["swing"]["stocks"] > 0
        assert data["etfs"]["etfs"] > 0

    def test_seed_idempotent(self):
        client.post("/api/seed")
        resp = client.post("/api/seed")
        assert resp.status_code == 200


class TestScreener:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_get_all_stocks(self):
        resp = client.get("/api/screener")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 0
        first = data[0]
        assert "symbol" in first
        assert "total_score" in first
        assert "verdict" in first

    def test_filter_by_sector(self):
        resp = client.get("/api/screener?sector=FMCG")
        assert resp.status_code == 200
        data = resp.json()
        assert all(s["sector"] == "FMCG" for s in data)

    def test_filter_by_min_score(self):
        resp = client.get("/api/screener?min_score=9")
        assert resp.status_code == 200
        data = resp.json()
        assert all(s["total_score"] >= 9 for s in data)

    def test_defensive_only(self):
        resp = client.get("/api/screener?defensive_only=true")
        assert resp.status_code == 200
        data = resp.json()
        assert all(s["is_defensive_sector"] for s in data)

    def test_sort_by_pe(self):
        resp = client.get("/api/screener?sort_by=pe")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 1

    def test_sort_by_roe(self):
        resp = client.get("/api/screener?sort_by=roe")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 1

    def test_sort_by_mcap(self):
        resp = client.get("/api/screener?sort_by=mcap")
        assert resp.status_code == 200

    def test_sort_by_margin_of_safety(self):
        resp = client.get("/api/screener?sort_by=mos")
        assert resp.status_code == 200


class TestStockDetail:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_get_stock(self):
        resp = client.get("/api/stock/TCS")
        assert resp.status_code == 200
        data = resp.json()
        assert data["symbol"] == "TCS"
        assert data["gdf_score"] is not None
        assert data["gdf_score"]["total_score"] >= 0

    def test_stock_has_all_signal_groups(self):
        resp = client.get("/api/stock/INFY")
        data = resp.json()
        gdf = data["gdf_score"]
        assert len(gdf["group_a"]) == 3
        assert len(gdf["group_b"]) == 3
        assert len(gdf["group_c"]) == 3
        assert len(gdf["group_d"]) == 3

    def test_stock_not_found(self):
        resp = client.get("/api/stock/NONEXIST")
        assert resp.status_code == 404

    def test_case_insensitive(self):
        resp = client.get("/api/stock/tcs")
        assert resp.status_code == 200
        assert resp.json()["symbol"] == "TCS"


class TestSectors:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_get_sectors(self):
        resp = client.get("/api/sectors")
        assert resp.status_code == 200
        sectors = resp.json()["sectors"]
        assert "FMCG" in sectors
        assert "Pharma" in sectors


class TestScoring:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_recompute_scores(self):
        resp = client.post("/api/score")
        assert resp.status_code == 200
        assert resp.json()["stocks_scored"] > 0

    def test_strong_stock_high_score(self):
        resp = client.get("/api/stock/HINDUNILVR")
        data = resp.json()
        assert data["gdf_score"]["total_score"] >= 7

    def test_weak_stock_low_score(self):
        resp = client.get("/api/stock/PAYTM")
        data = resp.json()
        assert data["gdf_score"]["total_score"] <= 4

    def test_verdict_mapping(self):
        resp = client.get("/api/screener")
        verdicts = {s["verdict"] for s in resp.json()}
        assert len(verdicts) > 1


class TestGrahamNumber:
    def test_graham_calculation(self):
        gn = compute_graham_number(50.0, 200.0)
        expected = math.sqrt(22.5 * 50.0 * 200.0)
        assert abs(gn - expected) < 0.01

    def test_negative_eps(self):
        assert compute_graham_number(-10.0, 200.0) == 0.0

    def test_negative_bv(self):
        assert compute_graham_number(50.0, -100.0) == 0.0

    def test_zero_eps(self):
        assert compute_graham_number(0.0, 200.0) == 0.0


class TestWatchlist:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_empty_watchlist(self):
        resp = client.get("/api/watchlist")
        assert resp.status_code == 200
        assert resp.json() == []

    def test_add_to_watchlist(self):
        resp = client.post("/api/watchlist", json={"symbol": "TCS", "notes": "Strong IT pick"})
        assert resp.status_code == 200
        assert resp.json()["symbol"] == "TCS"

    def test_add_invalid_stock(self):
        resp = client.post("/api/watchlist", json={"symbol": "FAKE"})
        assert resp.status_code == 404

    def test_add_duplicate(self):
        client.post("/api/watchlist", json={"symbol": "TCS"})
        resp = client.post("/api/watchlist", json={"symbol": "TCS"})
        assert resp.status_code == 409

    def test_remove_from_watchlist(self):
        client.post("/api/watchlist", json={"symbol": "INFY"})
        resp = client.delete("/api/watchlist/INFY")
        assert resp.status_code == 200
        assert resp.json()["status"] == "removed"

    def test_remove_nonexistent(self):
        resp = client.delete("/api/watchlist/FAKE")
        assert resp.status_code == 404

    def test_watchlist_persists(self):
        client.post("/api/watchlist", json={"symbol": "TCS"})
        client.post("/api/watchlist", json={"symbol": "INFY"})
        resp = client.get("/api/watchlist")
        assert len(resp.json()) == 2


class TestSwingAPI:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_get_swing_stocks(self):
        resp = client.get("/api/swing/stocks")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 0
        assert "total_score" in data[0]
        assert "signal" in data[0]

    def test_swing_stock_detail(self):
        resp = client.get("/api/swing/stock/RELIANCE")
        assert resp.status_code == 200
        data = resp.json()
        assert data["symbol"] == "RELIANCE"
        assert "score" in data
        assert data["score"]["total_score"] >= 0

    def test_swing_stock_not_found(self):
        resp = client.get("/api/swing/stock/NONEXIST")
        assert resp.status_code == 404

    def test_filter_by_signal(self):
        resp = client.get("/api/swing/stocks?signal=BUY")
        assert resp.status_code == 200
        data = resp.json()
        assert all(s["signal"] == "BUY" for s in data)

    def test_strong_setup_high_score(self):
        resp = client.get("/api/swing/stock/HDFCBANK")
        data = resp.json()
        assert data["score"]["total_score"] >= 6


class TestETFAPI:
    @pytest.fixture(autouse=True)
    def seed(self):
        client.post("/api/seed")

    def test_get_etfs(self):
        resp = client.get("/api/etf/list")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 0
        assert "total_score" in data[0]
        assert "verdict" in data[0]

    def test_etf_detail(self):
        resp = client.get("/api/etf/detail/NIFTYBEES")
        assert resp.status_code == 200
        data = resp.json()
        assert data["symbol"] == "NIFTYBEES"
        assert "score" in data

    def test_etf_not_found(self):
        resp = client.get("/api/etf/detail/NONEXIST")
        assert resp.status_code == 404

    def test_etf_categories(self):
        resp = client.get("/api/etf/categories")
        assert resp.status_code == 200
        cats = resp.json()["categories"]
        assert len(cats) > 0

    def test_filter_by_category(self):
        resp = client.get("/api/etf/list?category=Gold")
        assert resp.status_code == 200
        data = resp.json()
        assert all(e["category"] == "Gold" for e in data)
