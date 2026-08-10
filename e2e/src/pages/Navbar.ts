import { Locator, Page } from "@playwright/test";

export class Navbar {
  readonly root: Locator;

  constructor(private readonly page: Page) {
    this.root = page.locator("nav.navbar");
  }

  link(name: string): Locator {
    return this.root.getByRole("link", { name, exact: true });
  }

  get brand(): Locator {
    return this.root.locator(".navbar-brand");
  }

  get signIn(): Locator {
    return this.link("Sign in");
  }

  get signUp(): Locator {
    return this.link("Sign up");
  }

  get newPost(): Locator {
    return this.root.getByRole("link", { name: /New Post/ });
  }

  get settings(): Locator {
    return this.root.getByRole("link", { name: /Settings/ });
  }

  profile(username: string): Locator {
    return this.link(username);
  }
}
