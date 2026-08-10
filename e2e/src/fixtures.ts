import { Page, test as base } from "@playwright/test";
import { ApiClient, AuthUser } from "./api";
import { newUser } from "./data";
import { ArticlePage } from "./pages/ArticlePage";
import { EditorPage } from "./pages/EditorPage";
import { HomePage } from "./pages/HomePage";
import { LoginPage } from "./pages/LoginPage";
import { Navbar } from "./pages/Navbar";
import { ProfilePage } from "./pages/ProfilePage";
import { RegisterPage } from "./pages/RegisterPage";
import { SettingsPage } from "./pages/SettingsPage";

/**
 * The frontend keeps the session in localStorage under the "user" key; there is
 * no cookie. Seeding it before the first navigation is how tests start logged in.
 */
export async function loginAs(page: Page, user: AuthUser): Promise<void> {
  const stored = {
    email: user.email,
    username: user.username,
    token: user.token,
    bio: user.bio ?? "",
    image: user.image ?? "",
  };
  await page.addInitScript((value) => {
    window.localStorage.setItem("user", value);
  }, JSON.stringify(stored));
}

export async function logout(page: Page): Promise<void> {
  await page.evaluate(() => window.localStorage.removeItem("user"));
}

interface Fixtures {
  api: ApiClient;
  /** A freshly registered user, not logged into the browser. */
  user: AuthUser;
  /** A second freshly registered user, for follow/authorization scenarios. */
  otherUser: AuthUser;
  /** `page` already carrying `user`'s session. */
  authedPage: Page;
  navbar: Navbar;
  homePage: HomePage;
  loginPage: LoginPage;
  registerPage: RegisterPage;
  settingsPage: SettingsPage;
  editorPage: EditorPage;
  articlePage: ArticlePage;
  profilePage: ProfilePage;
}

export const test = base.extend<Fixtures>({
  // Profile images fall back to a remote placeholder host; block third-party
  // requests so runs stay fast and deterministic (the app renders a data-URI
  // placeholder instead).
  page: async ({ page }, use) => {
    await page.route("**/*", (route) => {
      const { hostname } = new URL(route.request().url());
      return hostname === "localhost" || hostname === "127.0.0.1" ? route.continue() : route.abort();
    });
    await use(page);
  },
  api: async ({ request }, use) => {
    await use(new ApiClient(request));
  },
  user: async ({ api }, use) => {
    await use(await api.register(newUser()));
  },
  otherUser: async ({ api }, use) => {
    await use(await api.register(newUser("e2eother")));
  },
  authedPage: async ({ page, user }, use) => {
    await loginAs(page, user);
    await use(page);
  },
  navbar: async ({ page }, use) => {
    await use(new Navbar(page));
  },
  homePage: async ({ page }, use) => {
    await use(new HomePage(page));
  },
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },
  registerPage: async ({ page }, use) => {
    await use(new RegisterPage(page));
  },
  settingsPage: async ({ page }, use) => {
    await use(new SettingsPage(page));
  },
  editorPage: async ({ page }, use) => {
    await use(new EditorPage(page));
  },
  articlePage: async ({ page }, use) => {
    await use(new ArticlePage(page));
  },
  profilePage: async ({ page }, use) => {
    await use(new ProfilePage(page));
  },
});

export { expect } from "@playwright/test";
