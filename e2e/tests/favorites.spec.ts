import { newArticle } from "../src/data";
import { expect, test } from "../src/fixtures";

test.describe("favorites and profiles", () => {
  test("a reader can favorite and unfavorite an article from the global feed", async ({
    api,
    user,
    otherUser,
    authedPage,
    homePage,
  }) => {
    const created = await api.createArticle(otherUser.token, newArticle());

    await homePage.goto();
    const favorite = homePage.favoriteButton(created.title);
    await expect(favorite).toHaveText("0");
    await expect(favorite).toHaveClass(/btn-outline-primary/);

    await favorite.click();
    await expect(favorite).toHaveText("1");
    await expect(favorite).toHaveClass(/btn-primary/);
    await expect
      .poll(async () => (await api.getArticle(created.slug, user.token)).favorited)
      .toBe(true);
    expect((await api.getArticle(created.slug, user.token)).favoritesCount).toBe(1);

    await favorite.click();
    await expect(favorite).toHaveText("0");
    await expect(favorite).toHaveClass(/btn-outline-primary/);
    await expect
      .poll(async () => (await api.getArticle(created.slug, user.token)).favorited)
      .toBe(false);
    expect((await api.getArticle(created.slug, user.token)).favoritesCount).toBe(0);
  });

  test("an anonymous visitor clicking favorite is sent to the login page", async ({
    api,
    user,
    page,
    homePage,
  }) => {
    const created = await api.createArticle(user.token, newArticle());

    await homePage.goto();
    await homePage.favoriteButton(created.title).click();

    await expect(page).toHaveURL(/\/user\/login$/);
  });

  test("favorited articles are listed on the reader's profile, separate from their own articles", async ({
    api,
    user,
    otherUser,
    authedPage,
    profilePage,
  }) => {
    const own = await api.createArticle(user.token, newArticle());
    const favorited = await api.createArticle(otherUser.token, newArticle());
    await api.favorite(user.token, favorited.slug);

    await profilePage.goto(user.username);
    await expect(profilePage.preview(own.title)).toBeVisible();
    await expect(profilePage.preview(favorited.title)).toHaveCount(0);

    await profilePage.favoritedArticlesTab.click();
    await expect(authedPage).toHaveURL(/favorite=true/);
    await expect(profilePage.preview(favorited.title)).toBeVisible();
    await expect(profilePage.preview(own.title)).toHaveCount(0);

    await profilePage.myArticlesTab.click();
    await expect(profilePage.preview(own.title)).toBeVisible();
    await expect(profilePage.preview(favorited.title)).toHaveCount(0);
  });

  test("another author's profile shows their details, articles and a follow button", async ({
    api,
    otherUser,
    authedPage,
    profilePage,
  }) => {
    const bio = `Bio of ${otherUser.username}`;
    await api.updateUser(otherUser.token, { bio });
    const article = await api.createArticle(otherUser.token, newArticle());

    await profilePage.goto(otherUser.username);
    await expect(profilePage.username).toHaveText(otherUser.username);
    await expect(profilePage.bio).toHaveText(bio);
    await expect(profilePage.preview(article.title)).toBeVisible();
    await expect(profilePage.followButton).toHaveText(`Follow ${otherUser.username}`);
    await expect(profilePage.editProfileLink).toHaveCount(0);
  });

  test("a logged-in reader can follow another author from their profile", async ({
    api,
    user,
    otherUser,
    authedPage,
    profilePage,
  }) => {
    expect((await api.getProfile(otherUser.username, user.token)).following).toBe(false);

    await profilePage.goto(otherUser.username);
    await profilePage.followButton.click();

    await expect
      .poll(async () => (await api.getProfile(otherUser.username, user.token)).following)
      .toBe(true);
  });

  test("a user sees the settings link and no follow button on their own profile", async ({
    user,
    authedPage,
    profilePage,
  }) => {
    await profilePage.goto(user.username);
    await expect(profilePage.followButton).toHaveCount(0);
    await expect(profilePage.editProfileLink).toBeVisible();

    await profilePage.editProfileLink.click();
    await expect(authedPage).toHaveURL(/\/user\/settings$/);
  });

  test("an anonymous visitor sees neither the follow button nor the settings link on a profile", async ({
    user,
    profilePage,
  }) => {
    await profilePage.goto(user.username);
    await expect(profilePage.username).toHaveText(user.username);
    await expect(profilePage.followButton).toHaveCount(0);
    await expect(profilePage.editProfileLink).toHaveCount(0);
  });
});
