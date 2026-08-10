import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";
import { NewUser } from "../data";

export class RegisterPage {
  readonly username: Locator;
  readonly email: Locator;
  readonly password: Locator;
  readonly submit: Locator;
  readonly errors: Locator;

  constructor(private readonly page: Page) {
    this.username = page.getByPlaceholder("Username");
    this.email = page.getByPlaceholder("Email");
    this.password = page.getByPlaceholder("Password", { exact: true });
    this.submit = page.getByRole("button", { name: "Sign up" });
    this.errors = page.locator("ul.error-messages li");
  }

  async goto(): Promise<void> {
    await visit(this.page, "/user/register");
  }

  async register(user: NewUser): Promise<void> {
    await this.username.fill(user.username);
    await this.email.fill(user.email);
    await this.password.fill(user.password);
    await this.submit.click();
  }
}
