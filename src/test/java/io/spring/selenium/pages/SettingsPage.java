package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page Object for the Settings page. */
public class SettingsPage extends BasePage {

  @FindBy(css = "input[placeholder='URL of profile picture']")
  private WebElement imageInput;

  @FindBy(css = "input[placeholder='Username']")
  private WebElement usernameInput;

  @FindBy(css = "textarea[placeholder='Short bio about you']")
  private WebElement bioInput;

  @FindBy(css = "input[placeholder='Email']")
  private WebElement emailInput;

  @FindBy(css = "input[placeholder='New Password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement updateButton;

  @FindBy(css = "h1")
  private WebElement pageTitle;

  public SettingsPage(WebDriver driver) {
    super(driver);
  }

  public String getPageTitle() {
    return getText(pageTitle);
  }

  public void enterImage(String imageUrl) {
    type(imageInput, imageUrl);
  }

  public void enterUsername(String username) {
    type(usernameInput, username);
  }

  public void enterBio(String bio) {
    type(bioInput, bio);
  }

  public void enterEmail(String email) {
    type(emailInput, email);
  }

  public void enterPassword(String password) {
    type(passwordInput, password);
  }

  public void clickUpdate() {
    click(updateButton);
  }

  public String getImageValue() {
    return waitForVisibility(imageInput).getAttribute("value");
  }

  public String getUsernameValue() {
    return waitForVisibility(usernameInput).getAttribute("value");
  }

  public String getBioValue() {
    return waitForVisibility(bioInput).getAttribute("value");
  }

  public String getEmailValue() {
    return waitForVisibility(emailInput).getAttribute("value");
  }

  public boolean isUpdateButtonDisplayed() {
    return isDisplayed(updateButton);
  }

  public void waitForPageLoad() {
    wait.until(ExpectedConditions.visibilityOf(updateButton));
  }
}
