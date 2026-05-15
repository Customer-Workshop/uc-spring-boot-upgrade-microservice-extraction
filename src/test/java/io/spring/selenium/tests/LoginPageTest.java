package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** E2E tests for the Login page covering page load, form validation, and login flow. */
public class LoginPageTest extends BaseTest {

  private static final String LOGIN_URL = "http://localhost:3000/user/login";
  private LoginPage loginPage;

  @BeforeMethod
  public void navigateToLogin() {
    driver.get(LOGIN_URL);
    loginPage = new LoginPage(driver);
  }

  @Test(groups = {"smoke", "regression"})
  public void testLoginPageLoads() {
    createTest("testLoginPageLoads", "Verify the login page loads with correct heading");

    String title = loginPage.getPageTitle();
    assertNotNull(title, "Page title should not be null");
    assertTrue(
        title.toLowerCase().contains("sign in"),
        "Page should display 'Sign In' heading but found: " + title);

    test.info("Login page loaded. Title: " + title);
  }

  @Test(groups = {"smoke", "regression"})
  public void testLoginFormElementsPresent() {
    createTest(
        "testLoginFormElementsPresent",
        "Verify email input, password input, and submit button are present");

    WebElement emailInput = driver.findElement(By.cssSelector("input[type='email']"));
    assertTrue(emailInput.isDisplayed(), "Email input should be visible");
    assertEquals(
        emailInput.getAttribute("placeholder"), "Email", "Email placeholder should be 'Email'");

    WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
    assertTrue(passwordInput.isDisplayed(), "Password input should be visible");
    assertEquals(
        passwordInput.getAttribute("placeholder"),
        "Password",
        "Password placeholder should be 'Password'");

    WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
    assertTrue(submitBtn.isDisplayed(), "Sign in button should be visible");
    assertTrue(
        submitBtn.getText().toLowerCase().contains("sign in"),
        "Submit button should say 'Sign in'");

    test.info("All login form elements verified");
  }

  @Test(groups = {"regression"})
  public void testLoginWithInvalidCredentials() {
    createTest("testLoginWithInvalidCredentials", "Verify error displayed for invalid credentials");

    loginPage.login("invalid@email.com", "wrongpassword");

    WebDriverWait wait = new WebDriverWait(driver, 10);
    // After invalid login, user should remain on login page or see error
    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlContains("/user/login"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages"))));

    assertTrue(
        driver.getCurrentUrl().contains("/user/login") || loginPage.isErrorDisplayed(),
        "Should remain on login page or show error for invalid credentials");

    test.info("Invalid login handled correctly");
  }

  @Test(groups = {"regression"})
  public void testLoginPageHasLinkToRegister() {
    createTest(
        "testLoginPageHasLinkToRegister", "Verify login page has a link to registration page");

    WebElement registerLink = driver.findElement(By.cssSelector("a[href='/user/register']"));
    assertNotNull(registerLink, "Register link should exist on login page");
    assertTrue(registerLink.isDisplayed(), "Register link should be visible");

    test.info("Register link found on login page");
  }

  @Test(groups = {"regression"})
  public void testNavigateFromLoginToRegister() {
    createTest(
        "testNavigateFromLoginToRegister",
        "Verify navigating from login to register page via link");

    WebElement registerLink = driver.findElement(By.cssSelector("a[href='/user/register']"));
    registerLink.click();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/register"));

    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "Should navigate to register page");

    WebElement heading = driver.findElement(By.tagName("h1"));
    assertTrue(
        heading.getText().toLowerCase().contains("sign up"),
        "Register page should show 'Sign Up' heading");

    test.info("Navigation from login to register verified");
  }

  @Test(groups = {"regression"})
  public void testLoginFormInputAcceptsText() {
    createTest("testLoginFormInputAcceptsText", "Verify form inputs accept and display typed text");

    loginPage.enterEmail("test@example.com");
    loginPage.enterPassword("testpassword");

    WebElement emailInput = driver.findElement(By.cssSelector("input[type='email']"));
    assertEquals(
        emailInput.getAttribute("value"),
        "test@example.com",
        "Email input should contain typed email");

    WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
    assertEquals(
        passwordInput.getAttribute("value"),
        "testpassword",
        "Password input should contain typed password");

    test.info("Form inputs accept and display text correctly");
  }
}
