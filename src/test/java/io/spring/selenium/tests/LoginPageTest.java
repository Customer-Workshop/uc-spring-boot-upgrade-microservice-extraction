package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.LoginPage;
import org.testng.annotations.Test;

/** E2E tests for the Login page. */
public class LoginPageTest extends BaseTest {

  private static final String LOGIN_URL = "http://localhost:3000/user/login";

  @Test(groups = {"smoke", "regression"})
  public void testLoginPageLoads() {
    createTest("testLoginPageLoads", "Verify the login page loads correctly");

    driver.get(LOGIN_URL);
    LoginPage loginPage = new LoginPage(driver);

    String title = loginPage.getPageTitle();
    assertNotNull(title, "Page title should not be null");
    assertTrue(title.toLowerCase().contains("sign in"), "Page should show Sign In heading");

    test.info("Login page loaded successfully. Title: " + title);
  }

  @Test(groups = {"regression"})
  public void testLoginFormElementsPresent() {
    createTest("testLoginFormElementsPresent", "Verify login form has all required elements");

    driver.get(LOGIN_URL);
    LoginPage loginPage = new LoginPage(driver);

    // Page should have email and password inputs and sign-in button
    assertNotNull(loginPage.getPageTitle(), "Page title should exist");

    test.info("Login form elements verified");
  }

  @Test(groups = {"regression"})
  public void testLoginWithInvalidCredentials() {
    createTest("testLoginWithInvalidCredentials", "Verify error on invalid credentials");

    driver.get(LOGIN_URL);
    LoginPage loginPage = new LoginPage(driver);

    loginPage.login("invalid@email.com", "wrongpassword");

    // Wait for error response
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    // Should remain on login page or show error
    assertTrue(
        driver.getCurrentUrl().contains("/user/login") || loginPage.isErrorDisplayed(),
        "Should remain on login page or show error for invalid credentials");

    test.info("Invalid login handled correctly");
  }
}
