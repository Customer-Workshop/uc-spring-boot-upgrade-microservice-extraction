import { newArticle } from "../src/data";
import { expect, test } from "../src/fixtures";

test.describe("smoke", () => {
  test("home page renders the global feed and popular tags", async ({ page, homePage, navbar }) => {
    await homePage.goto();
    await expect(navbar.brand).toHaveText("conduit");
    await expect(homePage.banner).toContainText("conduit");
    await expect(homePage.articlePreviews.first()).toBeVisible();
    await expect(page.getByText("Popular Tags")).toBeVisible();
    await expect(homePage.popularTags.first()).toBeVisible();
  });

  test("anonymous visitor sees sign in / sign up and no authoring links", async ({ homePage, navbar }) => {
    await homePage.goto();
    await expect(navbar.signIn).toBeVisible();
    await expect(navbar.signUp).toBeVisible();
    await expect(navbar.newPost).toHaveCount(0);
    await expect(navbar.settings).toHaveCount(0);
  });

  test("an article created through the API is readable in the UI", async ({
    api,
    user,
    articlePage,
  }) => {
    const article = newArticle({ tagList: ["smoke"] });
    const created = await api.createArticle(user.token, article);

    await articlePage.goto(created.slug);
    await expect(articlePage.title).toHaveText(article.title);
    await expect(articlePage.body).toContainText("With a second paragraph.");
    await expect(articlePage.tagList).toHaveText(["smoke"]);
    await expect(articlePage.author).toHaveText(user.username);
    await expect(articlePage.signInPrompt).toBeVisible();
  });

  test("a logged-in author sees edit/delete controls on their article", async ({
    api,
    user,
    authedPage,
    articlePage,
    navbar,
  }) => {
    const created = await api.createArticle(user.token, newArticle());

    await articlePage.goto(created.slug);
    await expect(navbar.profile(user.username)).toBeVisible();
    await expect(articlePage.editLink).toBeVisible();
    await expect(articlePage.deleteButton).toBeVisible();
    await expect(authedPage.getByPlaceholder("Write a comment...")).toBeVisible();
  });
});
