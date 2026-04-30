"""API integration tests for the Kite Trading Platform."""


class TestHealth:
    def test_root(self, client):
        resp = client.get("/")
        assert resp.status_code == 200
        data = resp.json()
        assert data["app"] == "Kite Trading Platform"

    def test_health(self, client):
        resp = client.get("/health")
        assert resp.status_code == 200
        assert resp.json()["status"] == "healthy"


class TestSeed:
    def test_seed_creates_instruments(self, client):
        resp = client.post("/api/seed")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "seeded"
        assert data["instruments"] == 45

    def test_seed_idempotent(self, seeded_client):
        resp = seeded_client.post("/api/seed")
        assert resp.status_code == 200
        data = resp.json()
        assert data["status"] == "already_seeded"


class TestDashboard:
    def test_dashboard_returns_data(self, seeded_client):
        resp = seeded_client.get("/api/dashboard")
        assert resp.status_code == 200
        data = resp.json()
        assert "indices" in data
        assert "holdings_count" in data
        assert data["holdings_count"] == 8
        assert data["positions_count"] == 3
        assert len(data["indices"]) == 3

    def test_dashboard_empty_without_seed(self, client):
        resp = client.get("/api/dashboard")
        assert resp.status_code == 200
        data = resp.json()
        assert data["holdings_count"] == 0


class TestInstruments:
    def test_search_instruments(self, seeded_client):
        resp = seeded_client.get("/api/instruments/search?q=RELIANCE")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) >= 1
        assert data[0]["symbol"] == "RELIANCE"

    def test_search_by_name(self, seeded_client):
        resp = seeded_client.get("/api/instruments/search?q=Infosys")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) >= 1
        assert data[0]["symbol"] == "INFY"

    def test_get_instrument(self, seeded_client):
        resp = seeded_client.get("/api/instruments/RELIANCE")
        assert resp.status_code == 200
        data = resp.json()
        assert data["symbol"] == "RELIANCE"
        assert data["name"] == "Reliance Industries Ltd"
        assert data["last_price"] > 0

    def test_get_instrument_not_found(self, seeded_client):
        resp = seeded_client.get("/api/instruments/NONEXISTENT")
        assert resp.status_code == 404

    def test_get_ohlcv(self, seeded_client):
        resp = seeded_client.get("/api/instruments/RELIANCE/ohlcv")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) > 200  # ~260 trading days in a year
        first = data[0]
        assert "date" in first
        assert "open" in first
        assert "close" in first

    def test_market_indices(self, seeded_client):
        resp = seeded_client.get("/api/market/indices")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) == 3
        names = [i["name"] for i in data]
        assert "NIFTY 50" in names
        assert "SENSEX" in names


class TestWatchlists:
    def test_get_watchlists(self, seeded_client):
        resp = seeded_client.get("/api/watchlists")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) == 3
        assert data[0]["name"] == "Watchlist 1"
        assert len(data[0]["items"]) == 8

    def test_add_item_to_watchlist(self, seeded_client):
        resp = seeded_client.post("/api/watchlists/1/items", json={"symbol": "WIPRO"})
        assert resp.status_code == 200
        data = resp.json()
        assert data["symbol"] == "WIPRO"

    def test_remove_item_from_watchlist(self, seeded_client):
        wl_resp = seeded_client.get("/api/watchlists")
        first_item_id = wl_resp.json()[0]["items"][0]["id"]
        resp = seeded_client.delete(f"/api/watchlists/1/items/{first_item_id}")
        assert resp.status_code == 200
        assert resp.json()["status"] == "deleted"


class TestOrders:
    def test_get_orders_empty(self, seeded_client):
        resp = seeded_client.get("/api/orders")
        assert resp.status_code == 200
        assert resp.json() == []

    def test_place_buy_order(self, seeded_client):
        order = {
            "symbol": "RELIANCE",
            "transaction_type": "BUY",
            "order_type": "MARKET",
            "product": "CNC",
            "quantity": 10,
        }
        resp = seeded_client.post("/api/orders", json=order)
        assert resp.status_code == 200
        data = resp.json()
        assert data["symbol"] == "RELIANCE"
        assert data["transaction_type"] == "BUY"
        assert data["status"] == "COMPLETE"
        assert data["filled_qty"] == 10
        assert data["average_price"] > 0

    def test_place_sell_order(self, seeded_client):
        order = {
            "symbol": "INFY",
            "transaction_type": "SELL",
            "order_type": "LIMIT",
            "product": "MIS",
            "quantity": 5,
            "price": 1450.0,
        }
        resp = seeded_client.post("/api/orders", json=order)
        assert resp.status_code == 200
        data = resp.json()
        assert data["transaction_type"] == "SELL"
        assert data["product"] == "MIS"

    def test_order_not_found_instrument(self, seeded_client):
        order = {
            "symbol": "NONEXISTENT",
            "transaction_type": "BUY",
            "quantity": 1,
        }
        resp = seeded_client.post("/api/orders", json=order)
        assert resp.status_code == 404

    def test_orders_appear_after_placement(self, seeded_client):
        seeded_client.post("/api/orders", json={
            "symbol": "TCS",
            "transaction_type": "BUY",
            "quantity": 5,
        })
        resp = seeded_client.get("/api/orders")
        assert len(resp.json()) == 1
        assert resp.json()[0]["symbol"] == "TCS"


class TestPortfolio:
    def test_get_holdings(self, seeded_client):
        resp = seeded_client.get("/api/holdings")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) == 8
        symbols = [h["symbol"] for h in data]
        assert "INFY" in symbols
        assert "RELIANCE" in symbols

    def test_get_positions(self, seeded_client):
        resp = seeded_client.get("/api/positions")
        assert resp.status_code == 200
        data = resp.json()
        assert len(data) == 3

    def test_get_funds(self, seeded_client):
        resp = seeded_client.get("/api/funds")
        assert resp.status_code == 200
        data = resp.json()
        assert data["equity_available"] > 0
        assert data["opening_balance"] > 0

    def test_buy_order_updates_holdings(self, seeded_client):
        seeded_client.post("/api/orders", json={
            "symbol": "RELIANCE",
            "transaction_type": "BUY",
            "product": "CNC",
            "quantity": 5,
        })
        resp = seeded_client.get("/api/holdings")
        reliance = [h for h in resp.json() if h["symbol"] == "RELIANCE"][0]
        assert reliance["quantity"] == 25  # 20 seeded + 5 bought

    def test_buy_order_updates_funds(self, seeded_client):
        initial = seeded_client.get("/api/funds").json()
        seeded_client.post("/api/orders", json={
            "symbol": "ITC",
            "transaction_type": "BUY",
            "product": "CNC",
            "quantity": 10,
        })
        updated = seeded_client.get("/api/funds").json()
        assert updated["equity_available"] < initial["equity_available"]
