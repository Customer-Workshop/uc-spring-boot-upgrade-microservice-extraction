import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";

export class LoginPage {
  readonly email: Locator;
  readonly password: Locator;
  readonly submit: Locator;
  readonly errors: Locator;

  constructor(private readonly page: Page) {
    this.email = page.getByPlaceholder("Email");
    this.password = page.getByPlaceholder("Password", { exact: true });
    this.submit = page.getByRole("button", { name: "Sign in" });
    this.errors = page.locator("ul.error-messages li");
  }

  async goto(): Promise<void> {
    await visit(this.page, "/user/login");
  }

  async login(email: string, password: string): Promise<void> {
    await this.email.fill(email);
    await this.password.fill(password);
    await this.submit.click();
  }
}
