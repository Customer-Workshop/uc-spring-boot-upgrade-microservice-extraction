package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends BasePage {

  @FindBy(css = "input[type='email']")
  private WebElement emailInput;

  @FindBy(css = "input[type='password']")
  private WebElement passwordInput;

  @FindBy(css = "button[type='submit']")
  private WebElement signInButton;

  @FindBy(css = ".error-messages li")
  private List<WebElement> errorMessages;

  public LoginPage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/user/login");
  }

  public void login(String email, String password) {
    type(emailInput, email);
    type(passwordInput, password);
    click(signInButton);
  }

  public List<String> getErrorMessages() {
    return errorMessages.stream().map(WebElement::getText).collect(Collectors.toList());
  }
}
