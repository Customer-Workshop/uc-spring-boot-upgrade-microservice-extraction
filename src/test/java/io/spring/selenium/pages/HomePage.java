package io.spring.selenium.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** Page object for the home page. */
public class HomePage extends BasePage {

  private final String baseUrl;

  @FindBy(css = ".home-page")
  private WebElement homePageContainer;

  public HomePage(WebDriver driver) {
    super(driver);
    this.baseUrl = loadBaseUrl();
  }

  public void navigateTo() {
    driver.get(baseUrl);
  }

  public void waitForPageLoad() {
    waitForVisibility(homePageContainer);
  }

  private String loadBaseUrl() {
    try {
      Properties config = new Properties();
      File configFile = new File("src/test/resources/selenium/config.properties");
      if (configFile.exists()) {
        config.load(new FileInputStream(configFile));
        return config.getProperty("base.url", "http://localhost:3000");
      }
    } catch (IOException e) {
      // fall through
    }
    return "http://localhost:3000";
  }
}
