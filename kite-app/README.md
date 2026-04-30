# Kite Trading Platform Clone

A Zerodha Kite-style trading web application with real-time watchlists, interactive charts, order placement, portfolio tracking, and fund management — built with mock data for demonstration purposes.

## Features

| Feature | Description |
|---------|-------------|
| **Dashboard** | Account summary with equity/commodity balances, holdings bar chart, market overview, positions P&L |
| **Watchlists** | 3 configurable watchlists with search, add/remove instruments, hover actions (Buy/Sell/Chart) |
| **Interactive Charts** | Full candlestick charts with volume bars via Lightweight Charts (TradingView) |
| **Order Placement** | Buy/Sell dialog with MIS/CNC/NRML products, MARKET/LIMIT/SL/SL-M order types |
| **Holdings** | Portfolio holdings with average cost, LTP, P&L, net change, day change |
| **Positions** | Open intraday/delivery positions with real-time P&L tracking |
| **Funds** | Available margins (equity/commodity), opening balance, collateral breakdown |
| **Search** | Universal instrument search across 40+ stocks, indices, and commodities |

## Tech Stack

- **Backend**: FastAPI + SQLAlchemy + Pydantic
- **Frontend**: Next.js 14 + TypeScript + Tailwind CSS + Lightweight Charts
- **Database**: PostgreSQL (production) / SQLite (local dev)
- **Container**: Docker + docker-compose

## Quick Start (Local Development)

```bash
# Backend
cd backend
pip install -r requirements.txt
uvicorn app.main:app --port 8001 --reload

# Seed mock data (in another terminal)
curl -X POST http://localhost:8001/api/seed

# Frontend
cd frontend
npm install
NEXT_PUBLIC_API_URL=http://localhost:8001 npm run dev
```

Open http://localhost:3002 to access the app.

## Docker

```bash
docker-compose up --build
# Then seed: curl -X POST http://localhost:8001/api/seed
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/dashboard` | Dashboard summary (indices, holdings, positions) |
| GET | `/api/instruments/search?q=` | Search instruments by symbol/name |
| GET | `/api/instruments/{symbol}` | Get instrument details |
| GET | `/api/instruments/{symbol}/ohlcv` | Get OHLCV price history |
| GET | `/api/market/indices` | Get market indices (NIFTY/SENSEX/BANKNIFTY) |
| GET | `/api/watchlists` | Get all watchlists with items |
| POST | `/api/watchlists/{id}/items` | Add instrument to watchlist |
| DELETE | `/api/watchlists/{id}/items/{item_id}` | Remove from watchlist |
| GET | `/api/orders` | Get order history |
| POST | `/api/orders` | Place new order (BUY/SELL) |
| GET | `/api/holdings` | Get portfolio holdings |
| GET | `/api/positions` | Get open positions |
| GET | `/api/funds` | Get fund/margin details |
| POST | `/api/seed` | Seed mock data (45 instruments, 1yr OHLCV) |

## Running Tests

```bash
cd backend
pytest tests/ -v
```

25 tests covering all API endpoints, order execution, portfolio updates, and fund management.

## Disclaimer

This is a research/demo tool, not investment advice. No real trades are executed. All data is mock/simulated.
