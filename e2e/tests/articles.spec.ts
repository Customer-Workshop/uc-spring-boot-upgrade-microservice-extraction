import { newArticle, uniqueTag } from "../src/data";
import { expect, loginAs, test } from "../src/fixtures";

test.describe("articles", () => {
  test("an author publishes an article from the editor and reads it back", async ({
    authedPage,
    editorPage,
    homePage,
    articlePage,
    user,
  }) => {
    const tags = [uniqueTag(), uniqueTag()];
    const article = newArticle({ tagList: tags });

    await editorPage.gotoNew();
    await editorPage.publishArticle(article);

    await expect(authedPage).toHaveURL(/\/$/);
    const preview = homePage.preview(article.title);
    await expect(preview).toBeVisible();
    await expect(preview).toContainText(article.description);

    await homePage.openArticle(article.title);
    await expect(articlePage.title).toHaveText(article.title);
    await expect(articlePage.body).toContainText("With a second paragraph.");
    await expect(articlePage.tagList).toHaveCount(tags.length);
    for (const tag of tags) {
      await expect(articlePage.tagList.filter({ hasText: tag })).toHaveCount(1);
    }
    await expect(articlePage.author).toHaveText(user.username);
  });

  test("tags typed in the editor become pills that can be removed before publishing", async ({
    authedPage,
    editorPage,
    homePage,
    articlePage,
  }) => {
    const kept = uniqueTag();
    const dropped = uniqueTag();
    const article = newArticle();

    await editorPage.gotoNew();
    await editorPage.fill(article);

    await editorPage.addTag(kept);
    await expect(editorPage.tagPill(kept)).toBeVisible();
    await expect(editorPage.tags).toHaveValue("");

    await editorPage.addTag(dropped);
    await expect(editorPage.tagPills).toHaveText([kept, dropped]);

    await editorPage.removeTag(dropped);
    await expect(editorPage.tagPills).toHaveText([kept]);

    await editorPage.publish.click();
    await expect(authedPage).toHaveURL(/\/$/);
    await homePage.openArticle(article.title);
    await expect(articlePage.tagList).toHaveText([kept]);
  });

  test("an author edits their article and the updated content replaces the old one", async ({
    api,
    user,
    authedPage,
    articlePage,
    editorPage,
    homePage,
  }) => {
    const original = newArticle({ tagList: [uniqueTag()] });
    const created = await api.createArticle(user.token, original);

    await articlePage.goto(created.slug);
    await articlePage.editLink.click();

    await expect(editorPage.title).toHaveValue(original.title);
    await expect(editorPage.description).toHaveValue(original.description);
    await expect(editorPage.body).toHaveValue(original.body);
    await expect(editorPage.tagPills).toHaveText(original.tagList);

    const updated = newArticle({ description: "revised description" });
    await editorPage.title.fill(updated.title);
    await editorPage.description.fill(updated.description);
    await editorPage.body.fill("Rewritten body paragraph.");
    await editorPage.update.click();

    await expect(authedPage).toHaveURL(/\/$/);
    await homePage.openArticle(updated.title);

    // The slug is derived from the title, so trust the link the UI followed.
    expect(authedPage.url()).not.toContain(created.slug);
    await expect(articlePage.title).toHaveText(updated.title);
    await expect(articlePage.body).toContainText("Rewritten body paragraph.");
    await expect(articlePage.body).not.toContainText("With a second paragraph.");
    await expect(homePage.preview(original.title)).toHaveCount(0);
  });

  test("an author deletes their article only after confirming the prompt", async ({
    api,
    user,
    authedPage,
    articlePage,
    homePage,
  }) => {
    const article = newArticle();
    const created = await api.createArticle(user.token, article);

    await articlePage.goto(created.slug);
    await articlePage.deleteArticle(false);
    await expect(articlePage.title).toHaveText(article.title);
    await expect(articlePage.deleteButton).toBeVisible();

    await articlePage.deleteArticle(true);
    await expect(authedPage).toHaveURL(/\/$/);
    await expect(homePage.articlePreviews.first()).toBeVisible();
    await expect(homePage.preview(article.title)).toHaveCount(0);

    await articlePage.goto(created.slug);
    await expect(articlePage.title).toHaveCount(0);
  });

  test("only the author sees the edit and delete controls on an article", async ({
    api,
    user,
    otherUser,
    page,
    articlePage,
  }) => {
    const created = await api.createArticle(user.token, newArticle());

    await loginAs(page, otherUser);
    await articlePage.goto(created.slug);
    await expect(articlePage.title).toBeVisible();
    await expect(articlePage.commentInput).toBeVisible();
    await expect(articlePage.editLink).toHaveCount(0);
    await expect(articlePage.deleteButton).toHaveCount(0);
  });

  test("an anonymous visitor sees no edit or delete controls on an article", async ({
    api,
    user,
    articlePage,
  }) => {
    const created = await api.createArticle(user.token, newArticle());

    await articlePage.goto(created.slug);
    await expect(articlePage.title).toBeVisible();
    await expect(articlePage.signInPrompt).toBeVisible();
    await expect(articlePage.editLink).toHaveCount(0);
    await expect(articlePage.deleteButton).toHaveCount(0);
  });
});
