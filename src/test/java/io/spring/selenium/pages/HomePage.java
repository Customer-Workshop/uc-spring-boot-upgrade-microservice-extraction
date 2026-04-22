package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HomePage extends BasePage {

  @FindBy(css = ".nav-pills .nav-link")
  private List<WebElement> feedTabs;

  @FindBy(css = ".article-preview h1")
  private List<WebElement> articleTitles;

  @FindBy(css = ".sidebar .tag-list .tag-pill")
  private List<WebElement> tags;

  @FindBy(css = ".navbar .nav-link")
  private List<WebElement> navLinks;

  public HomePage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl) {
    driver.get(baseUrl + "/");
  }

  public List<String> getArticleTitles() {
    return articleTitles.stream().map(WebElement::getText).collect(Collectors.toList());
  }

  public void clickTag(String tagName) {
    for (WebElement tag : tags) {
      if (tag.getText().trim().equals(tagName)) {
        click(tag);
        return;
      }
    }
  }

  public boolean isLoggedIn() {
    for (WebElement navLink : navLinks) {
      if (navLink.getText().contains("Sign in")) {
        return false;
      }
    }
    return true;
  }

  public String getNavUsername() {
    List<WebElement> profileLinks = driver.findElements(By.cssSelector(".navbar .nav-link"));
    if (!profileLinks.isEmpty()) {
      return profileLinks.get(profileLinks.size() - 1).getText().trim();
    }
    return null;
  }

  public boolean isGlobalFeedTabVisible() {
    for (WebElement tab : feedTabs) {
      if (tab.getText().contains("Global Feed")) {
        return true;
      }
    }
    return false;
  }

  public boolean isNavLinkVisible(String linkText) {
    for (WebElement navLink : navLinks) {
      if (navLink.getText().trim().contains(linkText)) {
        return true;
      }
    }
    return false;
  }
}
