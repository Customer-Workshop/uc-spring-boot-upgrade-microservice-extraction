package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/** Page Object for the Home / landing page. */
public class HomePage extends BasePage {

  @FindBy(css = ".navbar-brand")
  private WebElement navbarBrand;

  @FindBy(css = "a[href='/user/login']")
  private WebElement signInLink;

  @FindBy(css = "a[href='/user/register']")
  private WebElement signUpLink;

  @FindBy(css = ".tag-list .tag-default")
  private List<WebElement> popularTags;

  @FindBy(css = ".article-preview")
  private List<WebElement> articlePreviews;

  @FindBy(css = ".nav-link")
  private List<WebElement> feedTabs;

  @FindBy(css = ".banner h1")
  private WebElement bannerTitle;

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public String getNavbarBrandText() {
    return getText(navbarBrand);
  }

  public void clickSignIn() {
    click(signInLink);
  }

  public void clickSignUp() {
    click(signUpLink);
  }

  public int getPopularTagsCount() {
    return popularTags.size();
  }

  public int getArticlePreviewCount() {
    return articlePreviews.size();
  }

  public boolean isBannerDisplayed() {
    return isDisplayed(bannerTitle);
  }

  public String getBannerTitle() {
    return getText(bannerTitle);
  }

  public boolean isSignInLinkDisplayed() {
    return isDisplayed(signInLink);
  }

  public boolean isSignUpLinkDisplayed() {
    return isDisplayed(signUpLink);
  }

  public List<WebElement> getFeedTabs() {
    return feedTabs;
  }
}
