# Playwright Non-LLM Test Automation Platform

A full-stack, browser-based test automation platform built with **Python/FastAPI/SQLite** that enables QA engineers to manage, record, execute, and report on both **Web (Playwright)** and **API (Swagger/OpenAPI)** test cases — all through a dark-themed web UI, with **zero LLM dependency at runtime**.

## Key Features

- **Web Test Automation** — Playwright-based recording, step editing, and data-driven execution
- **API Test Automation** — Swagger/OpenAPI 2.0 + 3.x parsing with automatic variant generation
- **Auto-Healing Execution** — Smart locator fallback when selectors break (data-testid → aria → CSS → XPath → text)
- **Object Spy** — Interactive element inspector for capturing page element properties
- **Real-Time SSE Streaming** — Live execution log streaming via Server-Sent Events
- **Scheduled Execution** — Cron-based test scheduling via APScheduler
- **Environment Management** — Multi-environment support with base URL override
- **Dark-Themed UI** — Modern responsive interface with custom design system

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Python 3.11+, FastAPI, Uvicorn |
| ORM / DB | SQLAlchemy 2.x + SQLite (PostgreSQL supported via `DATABASE_URL`) |
| Templating | Jinja2 |
| Web Automation | Playwright (sync API) |
| API Testing | `requests` library |
| State Machine | `transitions` library |
| Swagger Parsing | `pyyaml` + `requests` |
| Frontend | Vanilla JS + Tailwind CSS (CDN) + custom CSS variables |
| Scheduling | APScheduler |

## Quick Start

```bash
# Install dependencies
python -m venv .venv
source .venv/bin/activate  # Linux/Mac
# .\.venv\Scripts\Activate.ps1  # Windows

pip install -r requirements.txt
playwright install chromium

# Start server (port 8005)
python -m uvicorn app.main:app --host 0.0.0.0 --port 8005 --reload
```

Open: http://localhost:8005

## Project Structure

```
├── app/
│   ├── main.py              # FastAPI app entry point
│   ├── routes.py            # All routes (40+ endpoints)
│   ├── scheduler.py         # APScheduler integration
│   ├── static/              # CSS & JS assets
│   └── templates/           # Jinja2 HTML templates
├── agent/
│   ├── state_machine.py     # FSM lifecycle management
│   ├── rule_engine.py       # Intent classification
│   ├── swagger_parser.py    # Swagger/OpenAPI parsing
│   ├── symbolic_runner.py   # Test execution dispatcher
│   ├── auto_healer.py       # Self-healing locator engine
│   └── object_spy.py        # Element inspector
├── api_integration/
│   └── executor.py          # API endpoint execution
├── db/
│   ├── models.py            # SQLAlchemy models (12 tables)
│   └── db_utils.py          # CRUD helpers
├── playwright_integration/
│   ├── recorder.py          # Playwright codegen integration
│   └── executor.py          # Step execution engine
├── tests/                   # Pytest test suite
├── requirements.txt
└── pytest.ini
```

## Auto-Healing

When a selector fails during execution, the auto-healer tries alternative strategies:

1. **data-testid** — Most stable, immune to styling changes
2. **aria-label / role** — Accessibility-based selectors
3. **ID / name attributes** — Classic DOM selectors
4. **CSS selector variants** — Parent-child relationships
5. **XPath** — Full path-based fallback
6. **Text content** — Visual text matching

Healing events are logged and suggestions are provided to update the original locator.

## Object Spy

The Object Spy captures all element properties from any page:

- All available selectors (CSS, XPath, data-testid, aria, etc.)
- Element attributes, dimensions, visibility state
- Recommended "best selector" based on stability scoring
- Bulk capture mode for entire page sections

## Running Tests

```bash
pytest tests/ -v --cov=agent --cov=api_integration --cov-report=term-missing
```

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DATABASE_URL` | `sqlite:///playwright_agent.db` | Database connection string |
| `SECRET_KEY` | (generated) | Session encryption key |
| `PORT` | `8005` | Server port |

## License

MIT
