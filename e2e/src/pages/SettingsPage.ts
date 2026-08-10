import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";
import { Navbar } from "./Navbar";

export class SettingsPage {
  readonly image: Locator;
  readonly username: Locator;
  readonly bio: Locator;
  readonly email: Locator;
  readonly password: Locator;
  readonly submit: Locator;
  readonly logout: Locator;

  constructor(private readonly page: Page) {
    this.image = page.getByPlaceholder("URL of profile picture");
    this.username = page.getByPlaceholder("Username");
    this.bio = page.getByPlaceholder("Short bio about you");
    this.email = page.getByPlaceholder("Email");
    this.password = page.getByPlaceholder("New Password");
    this.submit = page.getByRole("button", { name: "Update Settings" });
    this.logout = page.getByRole("button", { name: /logout/i });
  }

  async goto(): Promise<void> {
    await visit(this.page, "/user/settings");
  }

  /**
   * The session lives in localStorage, so a direct request to /user/settings is
   * redirected to the home page server-side; reach the page through the navbar.
   */
  async open(): Promise<void> {
    await new Navbar(this.page).settings.click();
  }
}
