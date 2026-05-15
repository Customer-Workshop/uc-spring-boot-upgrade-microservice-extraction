package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page Object for the User Profile page. */
public class ProfilePage extends BasePage {

  @FindBy(css = "h4")
  private WebElement username;

  @FindBy(css = ".user-info p")
  private WebElement bio;

  @FindBy(css = ".user-info img")
  private WebElement profileImage;

  @FindBy(css = ".article-preview")
  private List<WebElement> articlePreviews;

  @FindBy(css = ".nav-link")
  private List<WebElement> profileTabs;

  @FindBy(css = "button.action-btn")
  private WebElement followButton;

  @FindBy(css = "a.btn-outline-secondary")
  private WebElement editProfileButton;

  public ProfilePage(WebDriver driver) {
    super(driver);
  }

  public String getUsername() {
    return getText(username);
  }

  public String getBio() {
    try {
      return getText(bio);
    } catch (Exception e) {
      return "";
    }
  }

  public boolean isProfileImageDisplayed() {
    return isDisplayed(profileImage);
  }

  public int getArticlePreviewCount() {
    return articlePreviews.size();
  }

  public List<WebElement> getProfileTabs() {
    return profileTabs;
  }

  public boolean isFollowButtonDisplayed() {
    return isDisplayed(followButton);
  }

  public String getFollowButtonText() {
    return getText(followButton);
  }

  public void clickFollow() {
    click(followButton);
  }

  public boolean isEditProfileButtonDisplayed() {
    return isDisplayed(editProfileButton);
  }

  public void clickEditProfile() {
    click(editProfileButton);
  }

  public void waitForPageLoad() {
    wait.until(ExpectedConditions.visibilityOf(username));
  }

  public void clickTab(String tabName) {
    for (WebElement tab : profileTabs) {
      if (tab.getText().contains(tabName)) {
        tab.click();
        break;
      }
    }
  }

  public boolean hasMyArticlesTab() {
    return profileTabs.stream().anyMatch(tab -> tab.getText().contains("My Articles"));
  }

  public boolean hasFavoritedArticlesTab() {
    return profileTabs.stream().anyMatch(tab -> tab.getText().contains("Favorited Articles"));
  }
}
