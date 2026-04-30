# NIFTY 500 Top-20 Stock Screener

A production-grade, research-only stock screener that ranks NIFTY 500 stocks using a multi-factor scoring model combining **fundamentals, valuation, technical indicators, pattern recognition, insider/promoter signals, and news/macro regime analysis**.

> **Disclaimer**: This is a research tool only — NOT investment advice. All data is for educational purposes.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Next.js)                     │
│  Dashboard │ Stock Detail │ Backtest │ Alerts/Watchlist   │
│  Port: 3001                                              │
└──────────────────────┬──────────────────────────────────┘
                       │ HTTP/REST
┌──────────────────────▼──────────────────────────────────┐
│                   Backend (FastAPI)                       │
│  /universe │ /rankings │ /stock │ /backtest │ /signals   │
│  Port: 8000                                              │
├──────────────────────────────────────────────────────────┤
│  Services:                                                │
│  • Indicator Engine (RSI, MACD, Bollinger, ATR, MA)      │
│  • Pattern Engine (Breakout, Trend, S/R, Gap)            │
│  • Scoring Engine (Hard Filters + Weighted Composite)     │
│  • Backtesting Engine (Walk-forward, Slippage)           │
│  • Data Ingestion (NIFTY500 CSV, OHLCV, Fundamentals)   │
├──────────────────────────────────────────────────────────┤
│  Celery Workers + Beat (async job scheduling)             │
└──────┬──────────────────┬───────────────────────────────┘
       │                  │
  ┌────▼────┐       ┌─────▼─────┐
  │ Postgres │       │   Redis    │
  │ (+ data) │       │  (cache)   │
  └──────────┘       └───────────┘
```

## Tech Stack

| Layer      | Technology                                           |
|------------|------------------------------------------------------|
| Frontend   | Next.js 14, TypeScript, Tailwind CSS, Lightweight Charts, Recharts, SWR |
| Backend    | FastAPI, Pydantic, SQLAlchemy 2.0, Celery, Redis     |
| Database   | PostgreSQL 16                                        |
| Container  | Docker + docker-compose                              |
| Indicators | ta (Technical Analysis library), pandas, numpy       |

## Features

### Scoring Model (0-100)

| Factor              | Weight | Inputs                                              |
|---------------------|--------|------------------------------------------------------|
| Fundamentals        | 35%    | ROE, ROCE, margins, growth CAGR, D/E, cashflows     |
| Valuation           | 20%    | PE/PB z-score vs 5Y, EV/EBITDA vs sector, PEG, FCF yield |
| Technical Trend     | 25%    | RSI, MACD, MA crossovers, relative strength, trend position |
| Pattern + Volume    | 10%    | Breakout detection, HH/HL structure, volume expansion |
| Insider/News/Macro  | 10%    | Promoter buys/sells, pledge %, news sentiment         |

### Hard Filters (configurable)
- Minimum avg daily traded value: ₹1Cr
- Maximum debt/equity ratio: 3.0x
- Maximum promoter pledge: 50%
- Minimum data completeness: 60%

### Backtesting Strategies
- **Breakout**: Consolidation range + volume expansion
- **Pullback to MA**: Price returns to 20-SMA in uptrend
- **RSI Mean Reversion**: RSI < 30 entry, > 60 exit
- **Trend Following**: 50/200 SMA golden/death cross

### UI Pages
1. **Dashboard**: Top-20 sortable table with sector filters, score badges, and "why ranked" explanations
2. **Stock Detail**: Candlestick chart + volume, fundamentals panel, technical indicators, promoter disclosures timeline, news sentiment cards, score breakdown
3. **Backtest**: Strategy selector, parameter configuration, equity curve with drawdown, trade list with P&L and R-multiples

## Quick Start

### Using Docker Compose (recommended)

```bash
cd nifty500-screener
cp .env.example .env
docker-compose up --build
```

- Frontend: http://localhost:3001
- Backend API: http://localhost:8000
- API Docs: http://localhost:8000/docs

### Manual Setup

#### Backend

```bash
cd nifty500-screener/backend
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Start PostgreSQL and Redis (use docker or local install)
# Then:
uvicorn app.main:app --reload --port 8000
```

#### Frontend

```bash
cd nifty500-screener/frontend
npm install
npm run dev
```

### Seed Mock Data

When no API keys are configured, the app runs with mock data. To seed:

```bash
# Via API
curl -X POST http://localhost:8000/signals/seed

# Then compute signals and scores
curl -X POST http://localhost:8000/signals/recompute
```

## API Endpoints

| Method | Endpoint                      | Description                        |
|--------|-------------------------------|------------------------------------|
| GET    | `/universe`                   | List all NIFTY 500 stocks          |
| GET    | `/universe/sectors`           | List distinct sectors              |
| POST   | `/universe/seed`              | Seed stock universe                |
| GET    | `/rankings/today`             | Today's top-N rankings             |
| GET    | `/rankings/history`           | Historical rankings                |
| GET    | `/stock/{symbol}/snapshot`    | Full stock snapshot                |
| GET    | `/stock/{symbol}/chart`       | OHLCV chart data                   |
| GET    | `/stock/{symbol}/fundamentals`| Fundamental data                   |
| GET    | `/stock/{symbol}/technicals`  | Technical signals                  |
| POST   | `/backtest/run`               | Run backtest with strategy         |
| POST   | `/signals/recompute`          | Recompute signals + scores         |
| POST   | `/signals/seed`               | Seed mock data                     |
| POST   | `/auth/register`              | Register user                      |
| POST   | `/auth/login`                 | Login                              |

## Environment Variables

See [`.env.example`](.env.example) for all configuration options.

## Running Tests

```bash
cd backend
pip install -r requirements.txt
pytest tests/ -v --tb=short
```

## License

Research purposes only. Not for redistribution of market data.
