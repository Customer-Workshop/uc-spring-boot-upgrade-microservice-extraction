package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.RegisterPage;
import io.spring.selenium.pages.SettingsPage;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import org.testng.annotations.Test;

public class AuthTest extends BaseTest {

  private String uniqueUsername() {
    return "user" + UUID.randomUUID().toString().substring(0, 8);
  }

  private String uniqueEmail() {
    return "user" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
  }

  private void registerUserViaApi(String username, String email, String password) throws Exception {
    String apiUrl = config.getProperty("api.url", "http://localhost:8080");
    URL url = new URL(apiUrl + "/users");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    String json =
        String.format(
            "{\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}}",
            username, email, password);

    try (OutputStream os = conn.getOutputStream()) {
      os.write(json.getBytes());
    }

    conn.getResponseCode();
    conn.disconnect();
  }

  @Test(groups = {"smoke", "regression"})
  public void testUserRegistration() {
    createTest("testUserRegistration", "Register a new user via UI");
    String baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String username = uniqueUsername();
    String email = uniqueEmail();

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register(username, email, "password123");

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    HomePage homePage = new HomePage(driver);
    assertTrue(homePage.isLoggedIn(), "User should be logged in after registration");
    test.info("User registered and redirected to home page");
  }

  @Test(groups = {"smoke", "regression"})
  public void testUserLogin() throws Exception {
    createTest("testUserLogin", "Login an existing user via UI");
    String baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String username = uniqueUsername();
    String email = uniqueEmail();
    String password = "password123";

    registerUserViaApi(username, email, password);

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email, password);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    HomePage homePage = new HomePage(driver);
    assertTrue(homePage.isLoggedIn(), "User should be logged in after login");
    test.info("User logged in successfully");
  }

  @Test(groups = {"smoke", "regression"})
  public void testLogout() throws Exception {
    createTest("testLogout", "Logout a logged-in user");
    String baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String username = uniqueUsername();
    String email = uniqueEmail();
    String password = "password123";

    registerUserViaApi(username, email, password);

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email, password);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    SettingsPage settingsPage = new SettingsPage(driver);
    settingsPage.navigateTo(baseUrl);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    settingsPage.logout();

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    HomePage homePage = new HomePage(driver);
    assertTrue(homePage.isNavLinkVisible("Sign in"), "Sign in link should be visible after logout");
    test.info("User logged out successfully");
  }

  @Test(groups = {"smoke", "regression"})
  public void testRegistrationValidation() {
    createTest("testRegistrationValidation", "Submit empty registration form");
    String baseUrl = config.getProperty("base.url", "http://localhost:3000");

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo(baseUrl);
    registerPage.register("", "", "");

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertFalse(
        registerPage.getErrorMessages().isEmpty(),
        "Error messages should be displayed for empty form");
    test.info("Validation errors displayed for empty registration form");
  }

  @Test(groups = {"smoke", "regression"})
  public void testLoginWithWrongPassword() throws Exception {
    createTest("testLoginWithWrongPassword", "Login with incorrect password");
    String baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String username = uniqueUsername();
    String email = uniqueEmail();

    registerUserViaApi(username, email, "correctpassword");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email, "wrongpassword");

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    String currentUrl = driver.getCurrentUrl();
    assertTrue(
        currentUrl.contains("/user/login"), "Should remain on login page with wrong password");
    test.info("Login failed with wrong password as expected");
  }
}
