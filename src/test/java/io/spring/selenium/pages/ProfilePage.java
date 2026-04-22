package io.spring.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ProfilePage extends BasePage {

  @FindBy(css = ".user-info h4")
  private WebElement usernameDisplay;

  @FindBy(css = ".user-info p")
  private WebElement bioDisplay;

  @FindBy(css = ".user-info button.action-btn")
  private WebElement followButton;

  public ProfilePage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl, String username) {
    driver.get(baseUrl + "/profile/" + username);
  }

  public String getUsername() {
    return getText(usernameDisplay);
  }

  public void clickFollow() {
    click(followButton);
  }

  public boolean isFollowing() {
    try {
      String text = followButton.getText();
      return text.contains("Unfollow");
    } catch (Exception e) {
      return false;
    }
  }
}
