import { Locator, Page } from "@playwright/test";
import { visit } from "../navigation";

export class ProfilePage {
  readonly username: Locator;
  readonly bio: Locator;
  readonly followButton: Locator;
  readonly editProfileLink: Locator;
  readonly myArticlesTab: Locator;
  readonly favoritedArticlesTab: Locator;
  readonly articlePreviews: Locator;

  constructor(private readonly page: Page) {
    this.username = page.locator(".user-info h4");
    this.bio = page.locator(".user-info p");
    this.followButton = page.getByRole("button", { name: /(Follow|Unfollow) / });
    this.editProfileLink = page.getByRole("link", { name: /Edit Profile Settings/ });
    this.myArticlesTab = page.getByRole("link", { name: "My Articles" });
    this.favoritedArticlesTab = page.getByRole("link", { name: "Favorited Articles" });
    this.articlePreviews = page.locator(".article-preview");
  }

  async goto(username: string): Promise<void> {
    await visit(this.page, `/profile/${encodeURIComponent(username)}`);
  }

  preview(title: string): Locator {
    return this.articlePreviews.filter({ hasText: title });
  }
}
