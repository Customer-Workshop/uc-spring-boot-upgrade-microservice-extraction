# End-to-end tests (Playwright)

Browser-level tests that drive the Next.js frontend against the Spring Boot API.

## Prerequisites

Both services must be running before the suite starts (`globalSetup` fails fast with a
clear message otherwise):

```bash
# terminal 1 — API on :8080 (recreates dev.db with seed data)
./gradlew bootRun

# terminal 2 — frontend on :3000
cd frontend && npm install && NODE_OPTIONS=--openssl-legacy-provider npm run dev
```

## Running

```bash
cd e2e
npm install
npx playwright install chromium   # first run only
npm test                  # headless
npm run test:headed       # watch it in a browser
npm run report            # open the HTML report of the last run
npx playwright test tests/auth.spec.ts -g "logout"   # single file / single test
```

Override the URLs with `E2E_FRONTEND_URL` / `E2E_API_URL` when the services are not on
their default ports.

## Layout

| Path | Purpose |
| --- | --- |
| `playwright.config.ts` | Single chromium project, serial execution, trace/video on failure |
| `src/globalSetup.ts` | Fails the run early if the API or frontend is not reachable |
| `src/api.ts` | `ApiClient` — REST helpers used to arrange state without the UI |
| `src/data.ts` | Factories producing unique users, articles and tags per run |
| `src/fixtures.ts` | `test` with `api`, `user`, `otherUser`, `authedPage` and page-object fixtures |
| `src/pages/*.ts` | Page objects (navbar, home, login, register, settings, editor, article, profile) |
| `tests/*.spec.ts` | Specs, one file per user-facing flow |

## Conventions

- **Never rely on seed data for mutations.** Register users and create articles via
  `api` / the factories in `src/data.ts` so specs stay independent and re-runnable
  against a database that already contains previous runs' data.
- **Log in by seeding the session**, not by driving the login form: the frontend stores
  the JWT in `localStorage.user`, which the `authedPage` fixture (and `loginAs`) sets
  before the first navigation. Only auth specs should exercise the actual form.
- **Assert through page objects.** Add a locator to the page object instead of
  hard-coding a CSS selector in a spec.
- The suite runs serially with one worker: the app is backed by a single SQLite file and
  the global feed / tag lists are shared state.
