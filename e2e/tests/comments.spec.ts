import { newArticle, uniqueTag } from "../src/data";
import { expect, test } from "../src/fixtures";
import { ArticlePage } from "../src/pages/ArticlePage";

function commentBody(label: string): string {
  return `Comment ${label} ${uniqueTag("c")}`;
}

test.describe("comments", () => {
  test("an anonymous visitor is prompted to sign in and still sees existing comments", async ({
    api,
    user,
    otherUser,
    articlePage,
  }) => {
    const article = await api.createArticle(user.token, newArticle());
    const body = commentBody("from another reader");
    await api.createComment(otherUser.token, article.slug, body);

    await articlePage.goto(article.slug);

    await expect(articlePage.signInPrompt).toContainText("Sign in");
    await expect(articlePage.signInPrompt).toContainText(
      "sign up to add comments on this article."
    );
    await expect(articlePage.commentInput).toHaveCount(0);
    await expect(articlePage.comment(body)).toBeVisible();
    await expect(articlePage.commentAuthor(body)).toHaveText(otherUser.username);
  });

  test("a logged-in reader posts a comment and the form is cleared", async ({
    api,
    user,
    authedPage,
    articlePage,
  }) => {
    const article = await api.createArticle(user.token, newArticle());
    const first = commentBody("first");
    const second = commentBody("second");

    await articlePage.goto(article.slug);
    await expect(articlePage.commentInput).toBeVisible();

    await articlePage.addComment(first);
    await expect(articlePage.comment(first)).toBeVisible();
    await expect(articlePage.commentAuthor(first)).toHaveText(user.username);
    await expect(articlePage.commentInput).toHaveValue("");

    await articlePage.addComment(second);
    await expect(articlePage.comment(second)).toBeVisible();
    await expect(articlePage.comment(first)).toBeVisible();
    await expect(articlePage.commentInput).toHaveValue("");
  });

  test("a posted comment survives a reload and is visible to anonymous visitors", async ({
    api,
    user,
    browser,
    authedPage,
    articlePage,
  }) => {
    const article = await api.createArticle(user.token, newArticle());
    const body = commentBody("persisted");

    await articlePage.goto(article.slug);
    await articlePage.addComment(body);
    await expect(articlePage.comment(body)).toBeVisible();

    await articlePage.reload();
    await expect(articlePage.comment(body)).toBeVisible();

    const anonymousContext = await browser.newContext();
    await anonymousContext.route("**/*", (route) => {
      const { hostname } = new URL(route.request().url());
      return hostname === "localhost" || hostname === "127.0.0.1" ? route.continue() : route.abort();
    });
    const anonymousArticlePage = new ArticlePage(await anonymousContext.newPage());
    await anonymousArticlePage.goto(article.slug);

    await expect(anonymousArticlePage.signInPrompt).toBeVisible();
    await expect(anonymousArticlePage.comment(body)).toBeVisible();
    await expect(anonymousArticlePage.commentAuthor(body)).toHaveText(user.username);
    await anonymousContext.close();
  });

  test("an author deletes their own comment with the trash icon", async ({
    api,
    user,
    articlePage,
    authedPage,
  }) => {
    const article = await api.createArticle(user.token, newArticle());
    const kept = commentBody("kept");
    const removed = commentBody("removed");
    await api.createComment(user.token, article.slug, kept);
    await api.createComment(user.token, article.slug, removed);

    await articlePage.goto(article.slug);
    await expect(articlePage.comment(removed)).toBeVisible();

    await articlePage.deleteComment(removed);

    await expect(articlePage.comment(removed)).toHaveCount(0);
    await expect(articlePage.comment(kept)).toBeVisible();

    const remaining = await api.listComments(article.slug);
    expect(remaining.map((comment) => comment.body)).toEqual([kept]);
  });

  test("a reader can only delete their own comments", async ({
    api,
    user,
    otherUser,
    articlePage,
    authedPage,
  }) => {
    const article = await api.createArticle(user.token, newArticle());
    const mine = commentBody("mine");
    const theirs = commentBody("theirs");
    await api.createComment(user.token, article.slug, mine);
    await api.createComment(otherUser.token, article.slug, theirs);

    await articlePage.goto(article.slug);

    await expect(articlePage.comment(theirs)).toBeVisible();
    await expect(articlePage.commentDeleteIcon(mine)).toHaveCount(1);
    await expect(articlePage.commentDeleteIcon(theirs)).toHaveCount(0);
  });
});
