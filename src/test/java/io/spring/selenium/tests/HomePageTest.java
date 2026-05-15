package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** E2E tests for the Home page covering banner, navigation, feed tabs, and tag sidebar. */
public class HomePageTest extends BaseTest {

  private static final String BASE_URL = "http://localhost:3000";
  private HomePage homePage;

  @BeforeMethod
  public void navigateToHome() {
    driver.get(BASE_URL);
    homePage = new HomePage(driver);
  }

  @Test(groups = {"smoke", "regression"})
  public void testHomePageLoads() {
    createTest("testHomePageLoads", "Verify the home page loads with banner and conduit branding");

    assertTrue(homePage.isBannerDisplayed(), "Banner should be displayed on home page");
    String title = homePage.getBannerTitle();
    assertNotNull(title, "Banner title should not be null");
    assertEquals(title.toLowerCase(), "conduit", "Banner title should be 'conduit'");

    test.info("Home page loaded successfully. Banner title: " + title);
  }

  @Test(groups = {"smoke", "regression"})
  public void testNavigationLinksForUnauthenticatedUser() {
    createTest(
        "testNavigationLinksForUnauthenticatedUser",
        "Verify Sign In and Sign Up links are visible for unauthenticated users");

    assertTrue(homePage.isSignInLinkDisplayed(), "Sign In link should be visible");
    assertTrue(homePage.isSignUpLinkDisplayed(), "Sign Up link should be visible");

    test.info("Navigation links for unauthenticated user verified");
  }

  @Test(groups = {"smoke", "regression"})
  public void testNavbarBrandIsConduit() {
    createTest("testNavbarBrandIsConduit", "Verify navbar brand text is 'conduit'");

    String brandText = homePage.getNavbarBrandText();
    assertNotNull(brandText, "Navbar brand should not be null");
    assertEquals(brandText.toLowerCase(), "conduit", "Navbar brand should be 'conduit'");

    test.info("Navbar brand: " + brandText);
  }

  @Test(groups = {"regression"})
  public void testGlobalFeedTabPresent() {
    createTest("testGlobalFeedTabPresent", "Verify Global Feed tab is present on home page");

    assertFalse(homePage.getFeedTabs().isEmpty(), "At least one feed tab should be present");
    boolean hasGlobalFeed =
        homePage.getFeedTabs().stream().anyMatch(tab -> tab.getText().contains("Global Feed"));
    assertTrue(hasGlobalFeed, "Global Feed tab should be present");

    test.info("Feed tabs count: " + homePage.getFeedTabs().size());
  }

  @Test(groups = {"smoke"})
  public void testNavigateToSignInPage() {
    createTest("testNavigateToSignInPage", "Verify clicking Sign In navigates to login page");

    homePage.clickSignIn();
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/login"));

    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Should navigate to login page");

    WebElement heading = driver.findElement(By.tagName("h1"));
    assertTrue(
        heading.getText().toLowerCase().contains("sign in"),
        "Login page heading should contain 'Sign In'");

    test.info("Successfully navigated to Sign In page: " + driver.getCurrentUrl());
  }

  @Test(groups = {"smoke"})
  public void testNavigateToSignUpPage() {
    createTest("testNavigateToSignUpPage", "Verify clicking Sign Up navigates to register page");

    homePage.clickSignUp();
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/register"));

    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "Should navigate to register page");

    WebElement heading = driver.findElement(By.tagName("h1"));
    assertTrue(
        heading.getText().toLowerCase().contains("sign up"),
        "Register page heading should contain 'Sign Up'");

    test.info("Successfully navigated to Sign Up page: " + driver.getCurrentUrl());
  }

  @Test(groups = {"regression"})
  public void testHomeNavLinkReturnsToHome() {
    createTest("testHomeNavLinkReturnsToHome", "Verify Home nav link returns to home page");

    homePage.clickSignIn();
    WebDriverWait wait = new WebDriverWait(driver, 10);
    wait.until(ExpectedConditions.urlContains("/user/login"));

    WebElement homeLink = driver.findElement(By.cssSelector("a.nav-link[href='/']"));
    homeLink.click();
    wait.until(ExpectedConditions.urlToBe(BASE_URL + "/"));

    assertTrue(homePage.isBannerDisplayed(), "Banner should be displayed after returning home");

    test.info("Home nav link navigation verified");
  }

  @Test(groups = {"regression"})
  public void testFooterIsDisplayed() {
    createTest("testFooterIsDisplayed", "Verify footer is displayed on the home page");

    WebElement footer = driver.findElement(By.tagName("footer"));
    assertNotNull(footer, "Footer should exist on the page");
    assertTrue(footer.isDisplayed(), "Footer should be displayed");

    test.info("Footer displayed successfully");
  }

  @Test(groups = {"regression"})
  public void testPageTitleIsSet() {
    createTest("testPageTitleIsSet", "Verify the browser page title is set");

    String pageTitle = driver.getTitle();
    assertNotNull(pageTitle, "Page title should not be null");
    assertFalse(pageTitle.isEmpty(), "Page title should not be empty");

    test.info("Page title: " + pageTitle);
  }
}
