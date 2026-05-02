# GDF-12 Defensive Stock Screener

A **Benjamin Graham–style** defensive stock screener for Indian equities using the **GDF-12 (Great Defensive Fundamental – 12 Signal Model)**.

Finds fundamentally strong stocks at the right price using 12 signals across 4 groups.

## GDF-12 Signal Model

### Group A — Business Quality (Non-Negotiable)
- **A1**: Large & Stable (Market Cap >₹5,000 Cr, Sales >₹1,000 Cr)
- **A2**: Consistent Profits (10 consecutive profitable years)
- **A3**: Economic Moat (brand, cost advantage, regulatory barrier, or market share)

### Group B — Financial Strength (Survival Test)
- **B4**: Low Debt (D/E <0.5 or Interest Coverage >4)
- **B5**: Healthy Returns (ROE ≥15%, ROCE ≥15%)
- **B6**: Strong Cash Flows (positive OCF 8+/10 years, tracks profit)

### Group C — Valuation
- **C7**: Reasonable PE (<25, below 5Y average)
- **C8**: Graham Value Check (Price ≤70% of Graham Number)
- **C9**: Price to Book (below sector average)

### Group D — Shareholder & Market Signals
- **D10**: Promoter Quality (>50% holding, minimal pledge)
- **D11**: Dividend Consistency (7-10 years)
- **D12**: Silent Accumulation (flat price + growing profits + rising volume)

### Verdict
- **≥10/12**: Strong Buy on Dips
- **9/12**: Quality Candidate
- **7-8/12**: Watch
- **5-6/12**: Weak — Needs Improvement
- **<5/12**: Avoid

## Quick Start

### Windows — Double-click to launch:
```
start.bat        # Launches everything
stop.bat         # Stops everything
```

### Mac/Linux:
```bash
./start.sh       # Launches everything
./stop.sh        # Stops everything
```

### Manual Launch

**Backend:**
```bash
cd backend
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8002 --reload
curl -X POST http://localhost:8002/api/seed
```

**Frontend:**
```bash
cd frontend
npm install
NEXT_PUBLIC_API_URL=http://localhost:8002 npm run dev
```

Open **http://localhost:3003**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| POST | `/api/seed` | Seed mock stock data + compute scores |
| GET | `/api/screener` | Screened stocks with filters |
| GET | `/api/stock/{symbol}` | Full stock detail + GDF-12 breakdown |
| GET | `/api/sectors` | Available sectors |
| POST | `/api/score` | Recompute GDF-12 scores |
| GET | `/api/watchlist` | Get watchlist |
| POST | `/api/watchlist` | Add to watchlist |
| DELETE | `/api/watchlist/{symbol}` | Remove from watchlist |

### Screener Query Params
- `sector` — Filter by sector (e.g., FMCG, Pharma)
- `min_score` — Minimum GDF-12 score (0-12)
- `defensive_only` — Only defensive sectors (FMCG, Pharma, IT, Utilities, Gas)
- `sort_by` — `score`, `pe`, `roe`, `mcap`, `mos`

## Tech Stack

- **Backend**: FastAPI + SQLAlchemy + SQLite
- **Frontend**: Next.js 14 + TypeScript + Tailwind CSS
- **Tests**: pytest (30+ tests)

## Disclaimer

This is a **research tool only** — not investment advice. Based on Benjamin Graham's defensive investor criteria. Always do your own due diligence before investing.
