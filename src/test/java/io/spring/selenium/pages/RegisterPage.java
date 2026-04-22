package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RegisterPage extends BasePage {

  @FindBy(css = "input[type='text'][placeholder='Username']")
  private WebElement usernameInput;

  @FindBy(css = "input[type='email']")
  private WebElement emailInput;

  @FindBy(css = "input[type='password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement signUpButton;

  @FindBy(css = ".error-messages li")
  private List<WebElement> errorMessages;

  public RegisterPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/user/register");
  }

  public void register(String username, String email, String password) {
    type(usernameInput, username);
    type(emailInput, email);
    type(passwordInput, password);
    click(signUpButton);
  }

  public List<String> getErrorMessages() {
    return errorMessages.stream().map(WebElement::getText).collect(Collectors.toList());
  }
}
