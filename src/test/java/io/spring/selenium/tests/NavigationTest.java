package io.spring.selenium.tests;

import static org.testng.Assert.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

/** E2E tests for site-wide navigation, URL routing, and browser history. */
public class NavigationTest extends BaseTest {

  private static final String BASE_URL = "http://localhost:3000";

  @Test(groups = {"smoke", "regression"})
  public void testPageTitleIsSet() {
    createTest("testPageTitleIsSet", "Verify page title is set on initial load");

    driver.get(BASE_URL);
    String title = driver.getTitle();
    assertNotNull(title, "Page title should not be null");
    assertFalse(title.isEmpty(), "Page title should not be empty");

    test.info("Page title: " + title);
  }

  @Test(groups = {"regression"})
  public void testNavigateToLoginAndBack() {
    createTest(
        "testNavigateToLoginAndBack",
        "Verify full navigation flow: Home -> Login -> Home using navbar");

    driver.get(BASE_URL);
    WebDriverWait wait = new WebDriverWait(driver, 10);

    // Navigate to login
    WebElement signInLink = driver.findElement(By.cssSelector("a[href='/user/login']"));
    signInLink.click();
    wait.until(ExpectedConditions.urlContains("/user/login"));
    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Should be on login page");

    // Verify login page content
    WebElement heading = driver.findElement(By.tagName("h1"));
    assertTrue(heading.getText().toLowerCase().contains("sign in"), "Should show Sign In heading");

    // Navigate back to home
    WebElement homeLink = driver.findElement(By.cssSelector("a.nav-link[href='/']"));
    homeLink.click();
    wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

    // Verify we're back home
    WebElement banner = driver.findElement(By.cssSelector(".banner h1"));
    assertTrue(banner.isDisplayed(), "Banner should be visible after returning home");

    test.info("Navigation flow: Home -> Login -> Home verified");
  }

  @Test(groups = {"regression"})
  public void testNavigateToRegisterAndBack() {
    createTest(
        "testNavigateToRegisterAndBack",
        "Verify full navigation flow: Home -> Register -> Home using navbar");

    driver.get(BASE_URL);
    WebDriverWait wait = new WebDriverWait(driver, 10);

    // Navigate to register
    WebElement signUpLink = driver.findElement(By.cssSelector("a[href='/user/register']"));
    signUpLink.click();
    wait.until(ExpectedConditions.urlContains("/user/register"));
    assertTrue(driver.getCurrentUrl().contains("/user/register"), "Should be on register page");

    // Verify register page content
    WebElement heading = driver.findElement(By.tagName("h1"));
    assertTrue(heading.getText().toLowerCase().contains("sign up"), "Should show Sign Up heading");

    // Navigate back to home
    WebElement homeLink = driver.findElement(By.cssSelector("a.nav-link[href='/']"));
    homeLink.click();
    wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

    WebElement banner = driver.findElement(By.cssSelector(".banner h1"));
    assertTrue(banner.isDisplayed(), "Banner should be visible after returning home");

    test.info("Navigation flow: Home -> Register -> Home verified");
  }

  @Test(groups = {"regression"})
  public void testDirectUrlNavigation() {
    createTest(
        "testDirectUrlNavigation",
        "Verify direct URL navigation to all public pages works correctly");

    WebDriverWait wait = new WebDriverWait(driver, 10);

    // Direct to login
    driver.get(BASE_URL + "/user/login");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Direct login URL should work");

    // Direct to register
    driver.get(BASE_URL + "/user/register");
    wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "Direct register URL should work");

    // Direct to home
    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".banner")));
    assertTrue(
        driver.getCurrentUrl().equals(BASE_URL + "/") || driver.getCurrentUrl().equals(BASE_URL),
        "Direct home URL should work");

    test.info("Direct URL navigation verified for all public pages");
  }

  @Test(groups = {"regression"})
  public void testBrowserBackButton() {
    createTest("testBrowserBackButton", "Verify browser back button returns to previous page");

    driver.get(BASE_URL);
    WebDriverWait wait = new WebDriverWait(driver, 10);

    // Navigate to login
    WebElement signInLink = driver.findElement(By.cssSelector("a[href='/user/login']"));
    signInLink.click();
    wait.until(ExpectedConditions.urlContains("/user/login"));

    // Press back
    driver.navigate().back();
    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlToBe(BASE_URL + "/"), ExpectedConditions.urlToBe(BASE_URL)));

    test.info("Browser back button navigation verified");
  }

  @Test(groups = {"regression"})
  public void testNavbarPersistsAcrossPages() {
    createTest("testNavbarPersistsAcrossPages", "Verify navbar is present on all pages");

    // Check home page
    driver.get(BASE_URL);
    WebElement navbar = driver.findElement(By.cssSelector(".navbar"));
    assertTrue(navbar.isDisplayed(), "Navbar should be visible on home page");

    // Check login page
    driver.get(BASE_URL + "/user/login");
    navbar = driver.findElement(By.cssSelector(".navbar"));
    assertTrue(navbar.isDisplayed(), "Navbar should be visible on login page");

    // Check register page
    driver.get(BASE_URL + "/user/register");
    navbar = driver.findElement(By.cssSelector(".navbar"));
    assertTrue(navbar.isDisplayed(), "Navbar should be visible on register page");

    test.info("Navbar persistence verified across multiple pages");
  }

  @Test(groups = {"regression"})
  public void testNavbarBrandLinksToHome() {
    createTest("testNavbarBrandLinksToHome", "Verify clicking navbar brand navigates to home");

    driver.get(BASE_URL + "/user/login");
    WebDriverWait wait = new WebDriverWait(driver, 10);

    WebElement brand = driver.findElement(By.cssSelector(".navbar-brand"));
    brand.click();

    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlToBe(BASE_URL + "/"), ExpectedConditions.urlToBe(BASE_URL)));

    test.info("Navbar brand click navigates to home");
  }
}
