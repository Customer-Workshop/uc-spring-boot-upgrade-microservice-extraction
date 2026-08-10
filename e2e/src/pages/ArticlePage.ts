import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";

export class ArticlePage {
  readonly title: Locator;
  readonly body: Locator;
  readonly tagList: Locator;
  readonly author: Locator;
  readonly editLink: Locator;
  readonly deleteButton: Locator;
  readonly followButton: Locator;
  readonly commentInput: Locator;
  readonly postComment: Locator;
  readonly comments: Locator;
  readonly signInPrompt: Locator;

  constructor(private readonly page: Page) {
    this.title = page.locator(".banner h1");
    this.body = page.locator(".article-content");
    this.tagList = page.locator(".article-content .tag-list li");
    this.author = page.locator(".banner .article-meta .author").first();
    this.editLink = page.getByRole("link", { name: /Edit Article/ });
    this.deleteButton = page.getByRole("button", { name: /Delete Article/ });
    this.followButton = page.getByRole("button", { name: /(Follow|Unfollow) / });
    this.commentInput = page.getByPlaceholder("Write a comment...");
    this.postComment = page.getByRole("button", { name: "Post Comment" });
    this.comments = page.locator(".card:not(.comment-form)");
    this.signInPrompt = page.getByText("to add comments on this article.");
  }

  async goto(slug: string): Promise<void> {
    await visit(this.page, `/article/${encodeURIComponent(slug)}`);
  }

  async reload(): Promise<void> {
    await this.page.reload({ waitUntil: "domcontentloaded" });
  }

  comment(body: string): Locator {
    return this.comments.filter({ hasText: body });
  }

  async addComment(body: string): Promise<void> {
    await this.commentInput.fill(body);
    await this.postComment.click();
  }

  commentAuthor(body: string): Locator {
    return this.comment(body).locator(".card-footer .comment-author").last();
  }

  commentDeleteIcon(body: string): Locator {
    return this.comment(body).locator(".mod-options i");
  }

  /**
   * The icon font is served from a blocked CDN, so the trash glyph renders with a
   * zero-sized box that cannot be hit with a real mouse click.
   */
  async deleteComment(body: string): Promise<void> {
    await this.commentDeleteIcon(body).dispatchEvent("click");
  }

  /** The delete-article flow goes through window.confirm. */
  async deleteArticle(accept = true): Promise<void> {
    this.page.once("dialog", (dialog) => (accept ? dialog.accept() : dialog.dismiss()));
    await this.deleteButton.click();
  }
}
