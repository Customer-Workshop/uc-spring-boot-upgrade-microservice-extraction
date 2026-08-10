import { NewUser, newUser } from "../src/data";
import { expect, test } from "../src/fixtures";
import { LoginPage } from "../src/pages/LoginPage";
import { Navbar } from "../src/pages/Navbar";

async function signInThroughForm(
  loginPage: LoginPage,
  navbar: Navbar,
  credentials: { email: string; password: string; username: string }
): Promise<void> {
  await loginPage.goto();
  await loginPage.login(credentials.email, credentials.password);
  await expect(navbar.profile(credentials.username)).toBeVisible();
}

test.describe("authentication", () => {
  test("a visitor can register through the sign up form and lands logged in on the home page", async ({
    page,
    registerPage,
    homePage,
    navbar,
  }) => {
    const visitor: NewUser = newUser("e2esignup");

    await registerPage.goto();
    await registerPage.register(visitor);

    await expect(page).toHaveURL("/");
    await expect(homePage.banner).toContainText("conduit");
    await expect(navbar.profile(visitor.username)).toBeVisible();
    await expect(navbar.newPost).toBeVisible();
    await expect(navbar.settings).toBeVisible();
    await expect(navbar.signIn).toHaveCount(0);
    await expect(navbar.signUp).toHaveCount(0);
  });

  test("registering with an email that is already taken shows the API validation error", async ({
    page,
    api,
    registerPage,
    navbar,
  }) => {
    const existing = await api.register(newUser("e2etaken"));
    const duplicate = { ...newUser("e2edup"), email: existing.email };

    await registerPage.goto();
    await registerPage.register(duplicate);

    await expect(registerPage.errors).toContainText([/email/i]);
    await expect(page).toHaveURL("/user/register");
    await expect(navbar.signUp).toBeVisible();
  });

  test("an existing user can sign in through the login form", async ({
    page,
    api,
    loginPage,
    homePage,
    navbar,
  }) => {
    const registered = await api.register(newUser("e2esignin"));

    await loginPage.goto();
    await loginPage.login(registered.email, registered.password);

    await expect(page).toHaveURL("/");
    await expect(homePage.banner).toContainText("conduit");
    await expect(navbar.profile(registered.username)).toBeVisible();
    await expect(navbar.signIn).toHaveCount(0);
  });

  test("a user can update their bio and username from the settings page", async ({
    page,
    api,
    loginPage,
    settingsPage,
    profilePage,
    navbar,
  }) => {
    const registered = await api.register(newUser("e2eprofile"));
    const newUsername = `${registered.username}renamed`;
    const bio = `Bio updated by the e2e suite for ${newUsername}`;

    await signInThroughForm(loginPage, navbar, registered);

    await settingsPage.open();
    await expect(settingsPage.username).toHaveValue(registered.username);
    await settingsPage.bio.fill(bio);
    await settingsPage.username.fill(newUsername);
    await settingsPage.submit.click();

    await expect(page).toHaveURL("/");
    await expect(navbar.profile(newUsername)).toBeVisible();

    await profilePage.goto(newUsername);
    await expect(profilePage.username).toHaveText(newUsername);
    await expect(profilePage.bio).toHaveText(bio);

    const profile = await api.getProfile(newUsername);
    expect(profile.bio).toBe(bio);
  });

  test("logging out from the settings page makes the visitor anonymous again", async ({
    page,
    api,
    loginPage,
    settingsPage,
    navbar,
  }) => {
    const registered = await api.register(newUser("e2elogout"));

    await signInThroughForm(loginPage, navbar, registered);

    await settingsPage.open();
    await settingsPage.logout.click();

    await expect(page).toHaveURL("/");
    await expect(navbar.signIn).toBeVisible();
    await expect(navbar.signUp).toBeVisible();
    await expect(navbar.settings).toHaveCount(0);
    await expect(navbar.profile(registered.username)).toHaveCount(0);

    await settingsPage.goto();
    await expect(page).toHaveURL("/");
    await expect(navbar.signIn).toBeVisible();
  });
});
