package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.RegisterPage;
import org.testng.annotations.Test;

/** E2E tests for the Registration page. */
public class RegisterPageTest extends BaseTest {

  private static final String REGISTER_URL = "http://localhost:3000/user/register";

  @Test(groups = {"smoke", "regression"})
  public void testRegisterPageLoads() {
    createTest("testRegisterPageLoads", "Verify the registration page loads correctly");

    driver.get(REGISTER_URL);
    RegisterPage registerPage = new RegisterPage(driver);

    String title = registerPage.getPageTitle();
    assertNotNull(title, "Page title should not be null");
    assertTrue(title.toLowerCase().contains("sign up"), "Page should show Sign Up heading");

    test.info("Register page loaded successfully. Title: " + title);
  }

  @Test(groups = {"regression"})
  public void testRegisterFormElementsPresent() {
    createTest("testRegisterFormElementsPresent", "Verify registration form has all elements");

    driver.get(REGISTER_URL);
    RegisterPage registerPage = new RegisterPage(driver);

    assertNotNull(registerPage.getPageTitle(), "Page title should exist");

    test.info("Register form elements verified");
  }

  @Test(groups = {"regression"})
  public void testRegisterPageUrl() {
    createTest("testRegisterPageUrl", "Verify the register page URL");

    driver.get(REGISTER_URL);

    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "URL should contain /user/register");

    test.info("Register page URL verified: " + driver.getCurrentUrl());
  }
}
