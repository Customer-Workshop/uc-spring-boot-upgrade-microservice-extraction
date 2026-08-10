import { defineConfig, devices } from "@playwright/test";

export const FRONTEND_URL = process.env.E2E_FRONTEND_URL ?? "http://localhost:3000";
export const API_URL = process.env.E2E_API_URL ?? "http://localhost:8080";

export default defineConfig({
  testDir: "./tests",
  globalSetup: "./src/globalSetup.ts",
  // The app shares one SQLite database, so tests mutate common state (tags,
  // global feed, follow counts). Every spec creates its own users/articles, but
  // list assertions still need a stable ordering: run files serially.
  fullyParallel: false,
  workers: 1,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: process.env.CI ? [["list"], ["html", { open: "never" }]] : [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: FRONTEND_URL,
    actionTimeout: 15_000,
    navigationTimeout: 30_000,
    trace: "retain-on-failure",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
});
