package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SettingsPage extends BasePage {

  @FindBy(css = "input[placeholder='URL of profile picture']")
  private WebElement imageUrlInput;

  @FindBy(css = "input[placeholder='Username']")
  private WebElement usernameInput;

  @FindBy(css = "textarea[placeholder='Short bio about you']")
  private WebElement bioTextarea;

  @FindBy(css = "input[type='email']")
  private WebElement emailInput;

  @FindBy(css = "input[type='password']")
  private WebElement passwordInput;

  @FindBy(css = "button.btn-primary")
  private WebElement updateButton;

  @FindBy(css = "button.btn-outline-danger")
  private WebElement logoutButton;

  public SettingsPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/user/settings");
  }

  public void updateSettings(String imageUrl, String username, String bio, String email,
      String password) {
    if (imageUrl != null) {
      type(imageUrlInput, imageUrl);
    }
    if (username != null) {
      type(usernameInput, username);
    }
    if (bio != null) {
      type(bioTextarea, bio);
    }
    if (email != null) {
      type(emailInput, email);
    }
    if (password != null) {
      type(passwordInput, password);
    }
    click(updateButton);
  }

  public void logout() {
    click(logoutButton);
  }
}
