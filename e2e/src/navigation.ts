import { Page } from "@playwright/test";

/**
 * `next dev` keeps a webpack-HMR long-poll open, so the `load` event (Playwright's
 * default `waitUntil`) never fires. Every navigation therefore waits for
 * `domcontentloaded`; SWR data is asserted through web-first `expect` assertions.
 */
export async function visit(page: Page, path: string): Promise<void> {
  await page.goto(path, { waitUntil: "domcontentloaded" });
}
