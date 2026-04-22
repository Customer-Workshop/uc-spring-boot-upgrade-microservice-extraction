package io.spring.selenium.pages;

import java.util.List;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ArticlePage extends BasePage {

  @FindBy(css = ".banner h1")
  private WebElement titleHeader;

  @FindBy(css = ".article-content div")
  private WebElement bodyContent;

  @FindBy(css = ".tag-list .tag-pill")
  private List<WebElement> tagList;

  @FindBy(css = "button.btn-outline-danger")
  private WebElement deleteButton;

  @FindBy(css = ".comment-form textarea")
  private WebElement commentTextarea;

  @FindBy(css = ".comment-form button[type='submit']")
  private WebElement commentSubmitButton;

  @FindBy(css = ".card .card-block p")
  private List<WebElement> commentBodies;

  @FindBy(css = "button.btn-outline-secondary")
  private WebElement followButton;

  @FindBy(css = "button.btn-outline-primary, button.btn-primary")
  private WebElement favoriteButton;

  public ArticlePage(WebDriver driver) {
    super(driver);
  }

  public void navigateTo(String baseUrl, String slug) {
    driver.get(baseUrl + "/article/" + slug);
  }

  public String getTitle() {
    return getText(titleHeader);
  }

  public String getBody() {
    return getText(bodyContent);
  }

  public List<String> getTags() {
    return tagList.stream().map(WebElement::getText).collect(Collectors.toList());
  }

  public void deleteArticle() {
    click(deleteButton);
  }

  public void addComment(String text) {
    type(commentTextarea, text);
    click(commentSubmitButton);
  }

  public void deleteComment(String text) {
    List<WebElement> cards = driver.findElements(By.cssSelector(".card"));
    for (WebElement card : cards) {
      try {
        WebElement body = card.findElement(By.cssSelector(".card-block p"));
        if (body.getText().contains(text)) {
          WebElement deleteIcon = card.findElement(By.cssSelector(".mod-options i.ion-trash-a"));
          click(deleteIcon);
          return;
        }
      } catch (Exception e) {
        continue;
      }
    }
  }

  public void clickFavorite() {
    click(favoriteButton);
  }

  public void clickFollow() {
    click(followButton);
  }
}
