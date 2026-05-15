package io.spring.selenium.pages;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Page Object for the Article Detail page. */
public class ArticleDetailPage extends BasePage {

  @FindBy(css = ".banner h1")
  private WebElement articleTitle;

  @FindBy(css = ".article-content")
  private WebElement articleContent;

  @FindBy(css = ".tag-list .tag-default")
  private List<WebElement> tags;

  @FindBy(css = ".comment-form textarea")
  private WebElement commentInput;

  @FindBy(css = ".comment-form button[type='submit']")
  private WebElement postCommentButton;

  @FindBy(css = ".card .card-block p")
  private List<WebElement> commentBodies;

  @FindBy(css = ".article-meta .author")
  private WebElement authorLink;

  @FindBy(css = ".btn-outline-secondary")
  private WebElement editArticleButton;

  @FindBy(css = ".btn-outline-danger")
  private WebElement deleteArticleButton;

  public ArticleDetailPage(WebDriver driver) {
    super(driver);
  }

  public String getArticleTitle() {
    return getText(articleTitle);
  }

  public boolean isArticleContentDisplayed() {
    return isDisplayed(articleContent);
  }

  public int getTagCount() {
    return tags.size();
  }

  public List<String> getTagTexts() {
    return tags.stream().map(WebElement::getText).collect(java.util.stream.Collectors.toList());
  }

  public void enterComment(String comment) {
    type(commentInput, comment);
  }

  public void clickPostComment() {
    click(postCommentButton);
  }

  public void postComment(String comment) {
    enterComment(comment);
    clickPostComment();
  }

  public int getCommentCount() {
    return commentBodies.size();
  }

  public String getAuthorName() {
    return getText(authorLink);
  }

  public void clickAuthor() {
    click(authorLink);
  }

  public boolean isEditButtonDisplayed() {
    return isDisplayed(editArticleButton);
  }

  public boolean isDeleteButtonDisplayed() {
    return isDisplayed(deleteArticleButton);
  }

  public boolean isCommentFormDisplayed() {
    return isDisplayed(commentInput);
  }

  public void waitForPageLoad() {
    wait.until(ExpectedConditions.visibilityOf(articleTitle));
  }

  public boolean hasSignInPrompt() {
    try {
      WebElement signInPrompt = driver.findElement(By.linkText("Sign in"));
      return signInPrompt.isDisplayed();
    } catch (Exception e) {
      return false;
    }
  }
}
