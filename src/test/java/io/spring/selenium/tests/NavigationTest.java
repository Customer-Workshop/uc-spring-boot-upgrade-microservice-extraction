package io.spring.selenium.tests;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

/** E2E tests for site-wide navigation. */
public class NavigationTest extends BaseTest {

  private static final String BASE_URL = "http://localhost:3000";

  @Test(groups = {"smoke", "regression"})
  public void testPageTitleIsSet() {
    createTest("testPageTitleIsSet", "Verify page title is set on load");

    driver.get(BASE_URL);

    String title = driver.getTitle();
    assertNotNull(title, "Page title should not be null");

    test.info("Page title: " + title);
  }

  @Test(groups = {"regression"})
  public void testNavigateToLoginAndBack() {
    createTest("testNavigateToLoginAndBack", "Verify navigation from home to login and back");

    driver.get(BASE_URL);
    String homeUrl = driver.getCurrentUrl();

    driver.get(BASE_URL + "/user/login");
    assertTrue(driver.getCurrentUrl().contains("/user/login"), "Should be on login page");

    driver.get(homeUrl);
    assertEquals(driver.getCurrentUrl(), homeUrl, "Should return to home page");

    test.info("Navigation flow: Home -> Login -> Home verified");
  }

  @Test(groups = {"regression"})
  public void testNavigateToRegisterAndBack() {
    createTest("testNavigateToRegisterAndBack", "Verify navigation from home to register and back");

    driver.get(BASE_URL);
    String homeUrl = driver.getCurrentUrl();

    driver.get(BASE_URL + "/user/register");
    assertTrue(driver.getCurrentUrl().contains("/user/register"), "Should be on register page");

    driver.get(homeUrl);
    assertEquals(driver.getCurrentUrl(), homeUrl, "Should return to home page");

    test.info("Navigation flow: Home -> Register -> Home verified");
  }

  @Test(groups = {"regression"})
  public void testDirectUrlNavigation() {
    createTest("testDirectUrlNavigation", "Verify direct URL navigation works");

    driver.get(BASE_URL + "/user/login");
    assertTrue(
        driver.getCurrentUrl().contains("/user/login"), "Direct navigation to login should work");

    driver.get(BASE_URL + "/user/register");
    assertTrue(
        driver.getCurrentUrl().contains("/user/register"),
        "Direct navigation to register should work");

    test.info("Direct URL navigation verified");
  }
}
