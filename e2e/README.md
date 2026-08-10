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

## Coverage

| Spec | Flows |
| --- | --- |
| `smoke.spec.ts` | Home feed + popular tags render, anonymous vs. authenticated navbar, article page reads back an API-created article |
| `auth.spec.ts` | Sign up, duplicate-email validation error, sign in, settings update (bio + username), logout and the anonymous `/user/settings` redirect |
| `articles.spec.ts` | Publish from the editor, tag pills add/remove, edit and republish, delete behind `window.confirm`, edit/delete visible to the author only |
| `comments.spec.ts` | Anonymous sign-in prompt, posting comments, persistence across reload, deleting own comment, delete control scoped to own comments |
| `feed.spec.ts` | Global feed ordering, popular-tag filtering (click and direct `?tag=` URL), `Your Feed` for followed authors, pagination to page two |
| `favorites.spec.ts` | Favorite/unfavorite from a preview, anonymous favorite redirects to login, favorited vs. own articles on the profile tabs, follow another author, own-profile controls |

## Known application defects (deliberately not asserted)

These behaviors are broken in the app, so no test locks them in. Fixing them should come with the
corresponding test.

- **Wrong-password login crashes the frontend.** `/users/login` returns `{"message": ...}` without an
  `errors` object, and `ListErrors` does `Object.keys(errors)` on `undefined`.
- **Changing the password from settings breaks login.** `UserService.updateUser` stores the new password
  without encoding it, so the next sign-in fails.
- **Follow/unfollow on a profile shows a stale label.** `handleFollow` does not await `UserAPI.follow`
  before revalidating, so the button often keeps reading `Follow`; `handleUnfollow` mutates
  `following: true`, and the profile's `getInitialProps` fetches unauthenticated, so a reload always
  renders `Follow`. The follow *effect* is asserted through the API instead.
- **New articles are not first in the global feed.** Flyway seeds `articles.created_at` as text while the
  app writes epoch millis, and SQLite orders integers before text, so seeded articles always sort first.
  Feed specs assert relative ordering among app-created articles.

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
