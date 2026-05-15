package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import org.testng.annotations.Test;

/** E2E tests for the Home page. */
public class HomePageTest extends BaseTest {

  private static final String BASE_URL = "http://localhost:3000";

  @Test(groups = {"smoke", "regression"})
  public void testHomePageLoads() {
    createTest("testHomePageLoads", "Verify the home page loads correctly");

    driver.get(BASE_URL);
    HomePage homePage = new HomePage(driver);

    assertTrue(homePage.isBannerDisplayed(), "Banner should be displayed on home page");
    String title = homePage.getBannerTitle();
    assertNotNull(title, "Banner title should not be null");
    assertTrue(title.length() > 0, "Banner title should not be empty");

    test.info("Home page loaded successfully. Banner title: " + title);
  }

  @Test(groups = {"smoke", "regression"})
  public void testNavigationLinksPresent() {
    createTest("testNavigationLinksPresent", "Verify navigation links are present");

    driver.get(BASE_URL);
    HomePage homePage = new HomePage(driver);

    assertTrue(homePage.isSignInLinkDisplayed(), "Sign In link should be visible");
    assertTrue(homePage.isSignUpLinkDisplayed(), "Sign Up link should be visible");

    test.info("Navigation links verified successfully");
  }

  @Test(groups = {"regression"})
  public void testNavbarBrand() {
    createTest("testNavbarBrand", "Verify navbar brand is displayed");

    driver.get(BASE_URL);
    HomePage homePage = new HomePage(driver);

    String brandText = homePage.getNavbarBrandText();
    assertNotNull(brandText, "Navbar brand should not be null");
    assertTrue(brandText.length() > 0, "Navbar brand should not be empty");

    test.info("Navbar brand: " + brandText);
  }

  @Test(groups = {"regression"})
  public void testGlobalFeedTabPresent() {
    createTest("testGlobalFeedTabPresent", "Verify Global Feed tab is present");

    driver.get(BASE_URL);
    HomePage homePage = new HomePage(driver);

    assertFalse(homePage.getFeedTabs().isEmpty(), "Feed tabs should be present");

    test.info("Feed tabs count: " + homePage.getFeedTabs().size());
  }

  @Test(groups = {"smoke"})
  public void testNavigateToSignIn() {
    createTest("testNavigateToSignIn", "Verify navigation to Sign In page");

    driver.get(BASE_URL);
    HomePage homePage = new HomePage(driver);

    homePage.clickSignIn();

    // Wait for navigation
    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Should navigate to login page");
    test.info("Successfully navigated to Sign In page");
  }

  @Test(groups = {"smoke"})
  public void testNavigateToSignUp() {
    createTest("testNavigateToSignUp", "Verify navigation to Sign Up page");

    driver.get(BASE_URL);
    HomePage homePage = new HomePage(driver);

    homePage.clickSignUp();

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertTrue(
        driver.getCurrentUrl().contains("/user/register"), "Should navigate to register page");
    test.info("Successfully navigated to Sign Up page");
  }
}
