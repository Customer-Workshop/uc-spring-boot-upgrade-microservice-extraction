package io.spring.selenium.pages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** Page object for the register page. */
public class RegisterPage extends BasePage {

  private final String baseUrl;

  @FindBy(css = "form")
  private WebElement registerForm;

  public RegisterPage(WebDriver driver) {
    super(driver);
    this.baseUrl = loadBaseUrl();
  }

  public void navigateTo() {
    driver.get(baseUrl + "/user/register");
  }

  public void waitForPageLoad() {
    waitForVisibility(registerForm);
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
