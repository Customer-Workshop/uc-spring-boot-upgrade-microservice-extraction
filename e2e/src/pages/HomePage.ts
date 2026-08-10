import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";

export class HomePage {
  readonly banner: Locator;
  readonly tabs: Locator;
  readonly globalFeedTab: Locator;
  readonly yourFeedTab: Locator;
  readonly popularTags: Locator;
  readonly articlePreviews: Locator;
  readonly pagination: Locator;

  constructor(private readonly page: Page) {
    this.banner = page.locator(".banner");
    this.tabs = page.locator(".feed-toggle .nav-item");
    this.globalFeedTab = page.getByRole("link", { name: "Global Feed" });
    this.yourFeedTab = page.getByRole("link", { name: "Your Feed" });
    this.popularTags = page.locator(".sidebar .tag-list a, .col-md-3 .tag-list a");
    this.articlePreviews = page.locator(".article-preview");
    this.pagination = page.locator("ul.pagination .page-item");
  }

  async goto(): Promise<void> {
    await visit(this.page, "/");
  }

  async gotoTag(tag: string): Promise<void> {
    await visit(this.page, `/?tag=${encodeURIComponent(tag)}`);
  }

  preview(title: string): Locator {
    return this.articlePreviews.filter({ hasText: title });
  }

  /** Favorite/unfavorite toggle inside a preview; its label is the count. */
  favoriteButton(title: string): Locator {
    return this.preview(title).locator("button");
  }

  async openArticle(title: string): Promise<void> {
    await this.preview(title).getByRole("heading", { name: title }).click();
  }
}
