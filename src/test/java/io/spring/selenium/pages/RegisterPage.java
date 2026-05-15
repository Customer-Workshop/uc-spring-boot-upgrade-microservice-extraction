package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** Page Object for the Registration page. */
public class RegisterPage extends BasePage {

  @FindBy(css = "input[type='text']")
  private WebElement usernameInput;

  @FindBy(css = "input[type='email']")
  private WebElement emailInput;

  @FindBy(css = "input[type='password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement signUpButton;

  @FindBy(css = "h1")
  private WebElement pageTitle;

  @FindBy(css = ".error-messages")
  private WebElement errorMessages;

  public RegisterPage(WebDriver driver) {
    super(driver);
  }

  public void enterUsername(String username) {
    type(usernameInput, username);
  }

  public void enterEmail(String email) {
    type(emailInput, email);
  }

  public void enterPassword(String password) {
    type(passwordInput, password);
  }

  public void clickSignUp() {
    click(signUpButton);
  }

  public void register(String username, String email, String password) {
    enterUsername(username);
    enterEmail(email);
    enterPassword(password);
    clickSignUp();
  }

  public String getPageTitle() {
    return getText(pageTitle);
  }

  public boolean isErrorDisplayed() {
    return isDisplayed(errorMessages);
  }
}
