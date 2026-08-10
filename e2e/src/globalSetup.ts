import { request } from "@playwright/test";
import { API_URL, FRONTEND_URL } from "../playwright.config";

async function assertReachable(name: string, url: string) {
  const context = await request.newContext();
  try {
    const response = await context.get(url, { timeout: 15_000 });
    if (!response.ok()) {
      throw new Error(`${name} at ${url} responded with ${response.status()}`);
    }
  } catch (error) {
    throw new Error(
      `${name} is not reachable at ${url}. Start it before running the E2E suite ` +
        `(backend: ./gradlew bootRun, frontend: cd frontend && npm run dev). Cause: ${error}`
    );
  } finally {
    await context.dispose();
  }
}

export default async function globalSetup() {
  await assertReachable("Backend API", `${API_URL}/tags`);
  await assertReachable("Frontend", FRONTEND_URL);
}
