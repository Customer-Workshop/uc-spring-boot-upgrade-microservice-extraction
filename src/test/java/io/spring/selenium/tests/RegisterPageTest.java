package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.RegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** E2E tests for the Registration page covering form elements, validation, and navigation. */
public class RegisterPageTest extends BaseTest {

  private static final String REGISTER_URL = "http://localhost:3000/user/register";
  private RegisterPage registerPage;

  @BeforeMethod
  public void navigateToRegister() {
    driver.get(REGISTER_URL);
    registerPage = new RegisterPage(driver);
  }

  @Test(groups = {"smoke", "regression"})
  public void testRegisterPageLoads() {
    createTest("testRegisterPageLoads", "Verify the registration page loads with correct heading");

    String title = registerPage.getPageTitle();
    assertNotNull(title, "Page title should not be null");
    assertTrue(
        title.toLowerCase().contains("sign up"),
        "Page should display 'Sign Up' heading but found: " + title);

    test.info("Register page loaded. Title: " + title);
  }

  @Test(groups = {"smoke", "regression"})
  public void testRegisterFormElementsPresent() {
    createTest(
        "testRegisterFormElementsPresent",
        "Verify username, email, password inputs and submit button are present");

    WebElement usernameInput = driver.findElement(By.cssSelector("input[type='text']"));
    assertTrue(usernameInput.isDisplayed(), "Username input should be visible");
    assertEquals(
        usernameInput.getAttribute("placeholder"),
        "Username",
        "Username placeholder should be 'Username'");

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
    assertTrue(submitBtn.isDisplayed(), "Sign up button should be visible");
    assertTrue(
        submitBtn.getText().toLowerCase().contains("sign up"),
        "Submit button should say 'Sign up'");

    test.info("All register form elements verified");
  }

  @Test(groups = {"regression"})
  public void testRegisterFormInputsAcceptText() {
    createTest(
        "testRegisterFormInputsAcceptText", "Verify all form inputs accept and display typed text");

    registerPage.enterUsername("testuser");
    registerPage.enterEmail("test@example.com");
    registerPage.enterPassword("password123");

    WebElement usernameInput = driver.findElement(By.cssSelector("input[type='text']"));
    assertEquals(
        usernameInput.getAttribute("value"),
        "testuser",
        "Username input should contain typed username");

    WebElement emailInput = driver.findElement(By.cssSelector("input[type='email']"));
    assertEquals(
        emailInput.getAttribute("value"),
        "test@example.com",
        "Email input should contain typed email");

    WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
    assertEquals(
        passwordInput.getAttribute("value"),
        "password123",
        "Password input should contain typed password");

    test.info("All register form inputs accept text correctly");
  }

  @Test(groups = {"regression"})
  public void testRegisterPageHasLinkToLogin() {
    createTest("testRegisterPageHasLinkToLogin", "Verify register page has a link to login page");

    WebElement loginLink = driver.findElement(By.cssSelector("a[href='/user/login']"));
    assertNotNull(loginLink, "Login link should exist on register page");
    assertTrue(loginLink.isDisplayed(), "Login link should be visible");

    test.info("Login link found on register page");
  }

  @Test(groups = {"regression"})
  public void testNavigateFromRegisterToLogin() {
    createTest(
        "testNavigateFromRegisterToLogin",
        "Verify navigating from register to login page via link");

    WebElement loginLink = driver.findElement(By.cssSelector("a[href='/user/login']"));
    loginLink.click();

    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/login"));

    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Should navigate to login page");

    WebElement heading = driver.findElement(By.tagName("h1"));
    assertTrue(
        heading.getText().toLowerCase().contains("sign in"),
        "Login page should show 'Sign In' heading");

    test.info("Navigation from register to login verified");
  }

  @Test(groups = {"regression"})
  public void testRegisterPageUrl() {
    createTest("testRegisterPageUrl", "Verify the register page URL is correct");

    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "URL should contain /user/register");

    test.info("Register page URL verified: " + driver.getCurrentUrl());
  }
}
