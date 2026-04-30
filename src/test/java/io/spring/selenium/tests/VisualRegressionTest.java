package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.RegisterPage;
import io.spring.selenium.utils.ComparisonResult;
import io.spring.selenium.utils.ScreenshotComparisonUtil;
import org.testng.annotations.Test;

/** Visual regression tests that compare page screenshots against stored baselines. */
public class VisualRegressionTest extends BaseTest {

  @Test(groups = {"visual", "regression"})
  public void testHomePageVisualRegression() throws Exception {
    createTest("testHomePageVisualRegression", "Visual regression check for home page");
    HomePage homePage = new HomePage(driver);
    homePage.navigateTo();
    homePage.waitForPageLoad();

    Thread.sleep(2000);

    ComparisonResult result = ScreenshotComparisonUtil.compareWithBaseline(driver, "home_page");

    test.info("Diff percentage: " + result.getDiffPercentage() + "%");
    if (result.getDiffPath() != null) {
      test.addScreenCaptureFromPath(result.getDiffPath(), "Diff Image");
    }
    test.addScreenCaptureFromPath(result.getActualPath(), "Actual Screenshot");

    assertTrue(
        result.isPassed(),
        "Visual regression detected on home page. Diff: "
            + result.getDiffPercentage()
            + "%. Review diff at: "
            + result.getDiffPath());
  }

  @Test(groups = {"visual", "regression"})
  public void testLoginPageVisualRegression() throws Exception {
    createTest("testLoginPageVisualRegression", "Visual regression check for login page");
    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo();
    loginPage.waitForPageLoad();

    Thread.sleep(2000);

    ComparisonResult result = ScreenshotComparisonUtil.compareWithBaseline(driver, "login_page");

    test.info("Diff percentage: " + result.getDiffPercentage() + "%");
    if (result.getDiffPath() != null) {
      test.addScreenCaptureFromPath(result.getDiffPath(), "Diff Image");
    }
    test.addScreenCaptureFromPath(result.getActualPath(), "Actual Screenshot");

    assertTrue(
        result.isPassed(),
        "Visual regression detected on login page. Diff: "
            + result.getDiffPercentage()
            + "%. Review diff at: "
            + result.getDiffPath());
  }

  @Test(groups = {"visual", "regression"})
  public void testRegisterPageVisualRegression() throws Exception {
    createTest("testRegisterPageVisualRegression", "Visual regression check for register page");
    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo();
    registerPage.waitForPageLoad();

    Thread.sleep(2000);

    ComparisonResult result = ScreenshotComparisonUtil.compareWithBaseline(driver, "register_page");

    test.info("Diff percentage: " + result.getDiffPercentage() + "%");
    if (result.getDiffPath() != null) {
      test.addScreenCaptureFromPath(result.getDiffPath(), "Diff Image");
    }
    test.addScreenCaptureFromPath(result.getActualPath(), "Actual Screenshot");

    assertTrue(
        result.isPassed(),
        "Visual regression detected on register page. Diff: "
            + result.getDiffPercentage()
            + "%. Review diff at: "
            + result.getDiffPath());
  }

  @Test(
      groups = {"visual-baseline"},
      enabled = false)
  public void updateAllBaselines() throws Exception {
    createTest("updateAllBaselines", "Update all visual regression baselines");

    HomePage homePage = new HomePage(driver);
    homePage.navigateTo();
    homePage.waitForPageLoad();
    Thread.sleep(2000);
    ScreenshotComparisonUtil.captureFullPageScreenshot(driver, "home_page");
    ScreenshotComparisonUtil.updateBaseline("home_page");
    test.info("Updated baseline for home_page");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo();
    loginPage.waitForPageLoad();
    Thread.sleep(2000);
    ScreenshotComparisonUtil.captureFullPageScreenshot(driver, "login_page");
    ScreenshotComparisonUtil.updateBaseline("login_page");
    test.info("Updated baseline for login_page");

    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.navigateTo();
    registerPage.waitForPageLoad();
    Thread.sleep(2000);
    ScreenshotComparisonUtil.captureFullPageScreenshot(driver, "register_page");
    ScreenshotComparisonUtil.updateBaseline("register_page");
    test.info("Updated baseline for register_page");
  }
}
