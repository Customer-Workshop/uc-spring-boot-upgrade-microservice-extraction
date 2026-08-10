import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";
import { NewArticle } from "../data";

export class EditorPage {
  readonly title: Locator;
  readonly description: Locator;
  readonly body: Locator;
  readonly tags: Locator;
  readonly tagPills: Locator;
  readonly publish: Locator;
  readonly errors: Locator;

  constructor(private readonly page: Page) {
    this.title = page.getByPlaceholder("Article Title");
    this.description = page.getByPlaceholder("What's this article about?");
    this.body = page.getByPlaceholder("Write your article (in markdown)");
    this.tags = page.getByPlaceholder("Enter tags");
    this.tagPills = page.locator(".tag-list .tag-pill");
    this.publish = page.getByRole("button", { name: "Publish Article" });
    this.errors = page.locator("ul.error-messages li");
  }

  async gotoNew(): Promise<void> {
    await visit(this.page, "/editor/new");
  }

  async gotoEdit(slug: string): Promise<void> {
    await visit(this.page, `/editor/${encodeURIComponent(slug)}`);
  }

  async fill(article: NewArticle): Promise<void> {
    await this.title.fill(article.title);
    await this.description.fill(article.description);
    await this.body.fill(article.body);
    for (const tag of article.tagList) {
      await this.tags.fill(tag);
      await this.tags.press("Enter");
    }
  }

  async publishArticle(article: NewArticle): Promise<void> {
    await this.fill(article);
    await this.publish.click();
  }
}
