import { newArticle, newUser, uniqueTag } from "../src/data";
import { expect, test } from "../src/fixtures";

test.describe("home feeds", () => {
  test("the global feed shows a newly published article, newest first", async ({
    api,
    user,
    homePage,
  }) => {
    const older = await api.createArticle(user.token, newArticle());
    const newest = newArticle();
    await api.createArticle(user.token, newest);

    await homePage.goto();

    await expect(homePage.preview(newest.title)).toBeVisible();
    await expect(homePage.previewAuthor(newest.title)).toHaveText(user.username);
    await expect(homePage.previewDescription(newest.title)).toHaveText(newest.description);

    const titles = await homePage.previewTitles();
    expect(titles.indexOf(newest.title)).toBeLessThan(titles.indexOf(older.title));
  });

  test("clicking a popular tag filters the feed down to articles carrying it", async ({
    api,
    user,
    page,
    homePage,
  }) => {
    const tag = uniqueTag();
    const tagged = await api.createArticle(user.token, newArticle({ tagList: [tag] }));
    const untagged = await api.createArticle(user.token, newArticle());

    await homePage.goto();
    await expect(homePage.popularTag(tag)).toBeVisible();
    await homePage.popularTag(tag).click();

    await expect(page).toHaveURL(new RegExp(`\\?tag=${tag}$`));
    await expect(homePage.tagTab(tag)).toHaveClass(/active/);
    await expect(homePage.preview(tagged.title)).toBeVisible();
    await expect(homePage.preview(untagged.title)).toHaveCount(0);
  });

  test("visiting a tag url directly opens the filtered feed on its own tab", async ({
    api,
    user,
    homePage,
  }) => {
    const tag = uniqueTag();
    const tagged = await api.createArticle(user.token, newArticle({ tagList: [tag] }));
    const untagged = await api.createArticle(user.token, newArticle());

    await homePage.gotoTag(tag);

    await expect(homePage.tagTab(tag)).toBeVisible();
    await expect(homePage.tagTab(tag)).toHaveClass(/active/);
    await expect(homePage.preview(tagged.title)).toBeVisible();
    await expect(homePage.preview(untagged.title)).toHaveCount(0);
  });

  test("Your Feed lists articles from followed authors only", async ({
    api,
    user,
    otherUser,
    authedPage,
    homePage,
  }) => {
    const stranger = await api.register(newUser("e2estranger"));
    await api.follow(user.token, otherUser.username);
    const followed = await api.createArticle(otherUser.token, newArticle());
    const unfollowed = await api.createArticle(stranger.token, newArticle());

    await homePage.goto();
    await expect(homePage.globalFeedTab).toBeVisible();
    await homePage.yourFeedTab.click();

    await expect(authedPage).toHaveURL(new RegExp(`\\?follow=${user.username}$`));
    await expect(homePage.yourFeedTab).toHaveClass(/active/);
    await expect(homePage.preview(followed.title)).toBeVisible();
    await expect(homePage.previewAuthor(followed.title)).toHaveText(otherUser.username);
    await expect(homePage.preview(unfollowed.title)).toHaveCount(0);
  });

  test("Your Feed is hidden from anonymous visitors", async ({ homePage }) => {
    await homePage.goto();

    await expect(homePage.globalFeedTab).toBeVisible();
    await expect(homePage.yourFeedTab).toHaveCount(0);
  });

  test("the global feed paginates and page two shows different articles", async ({
    api,
    user,
    homePage,
  }) => {
    for (let i = 0; i < 21; i += 1) {
      await api.createArticle(user.token, newArticle());
    }

    await homePage.goto();
    await expect(homePage.paginationLink(1)).toBeVisible();
    await expect(homePage.paginationLink(2)).toBeVisible();
    await expect(homePage.paginationPage(1)).toHaveClass(/active/);

    const firstPageTitles = await homePage.previewTitles();
    expect(firstPageTitles).toHaveLength(20);

    await homePage.paginationLink(2).click();
    await expect(homePage.paginationPage(2)).toHaveClass(/active/);
    await expect(homePage.preview(firstPageTitles[0])).toHaveCount(0);

    const secondPageTitles = await homePage.previewTitles();
    expect(secondPageTitles.length).toBeGreaterThan(0);
    expect(secondPageTitles.filter((title) => firstPageTitles.includes(title))).toEqual([]);
  });
});
