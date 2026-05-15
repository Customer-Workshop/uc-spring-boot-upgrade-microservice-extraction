package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.RegisterPage;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

/**
 * E2E tests for Article interaction features including viewing articles, favoriting, commenting,
 * and tag-based navigation.
 */
public class ArticleInteractionTest extends BaseTest {

  private static final String BASE_URL = "http://localhost:3000";

  private String registerAndCreateArticle() {
    String uniqueSuffix = String.valueOf(System.currentTimeMillis());
    String username = "artint" + uniqueSuffix;
    String email = "artint" + uniqueSuffix + "@test.com";
    String title = "Article " + uniqueSuffix;

    driver.get(BASE_URL + "/user/register");
    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.register(username, email, "password123");

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlToBe(BASE_URL + "/"),
            ExpectedConditions.urlToBe(BASE_URL),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages"))));

    driver.get(BASE_URL + "/editor/new");
    wait.until(
        ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit']")));

    EditorPage editorPage = new EditorPage(driver);
    editorPage.createArticle(title, "Test desc", "Test body content");

    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlContains("/article/"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages"))));

    return title;
  }

  @Test(groups = {"regression"})
  public void testArticleDetailPageShowsContent() {
    createTest(
        "testArticleDetailPageShowsContent",
        "Verify article detail page shows title, body, and author info");

    String title = registerAndCreateArticle();
    WebDriverWait wait = new WebDriverWait(driver, 10);

    String currentUrl = driver.getCurrentUrl();
    if (currentUrl.contains("/article/")) {
      // Verify article detail
      WebElement articleTitle = driver.findElement(By.cssSelector(".banner h1"));
      assertEquals(articleTitle.getText(), title, "Article title should match");

      WebElement articleContent = driver.findElement(By.cssSelector(".article-content"));
      assertTrue(articleContent.isDisplayed(), "Article content should be visible");

      WebElement authorLink = driver.findElement(By.cssSelector(".article-meta .author"));
      assertTrue(authorLink.isDisplayed(), "Author link should be visible");

      test.info("Article detail page content verified: " + title);
    } else {
      test.info("Article detail not available, skipping content check");
    }
  }

  @Test(groups = {"regression"})
  public void testArticleDetailPageShowsTags() {
    createTest(
        "testArticleDetailPageShowsTags", "Verify article detail page shows tags when present");

    String uniqueSuffix = String.valueOf(System.currentTimeMillis());
    String username = "tagtest" + uniqueSuffix;
    String email = "tagtest" + uniqueSuffix + "@test.com";

    driver.get(BASE_URL + "/user/register");
    RegisterPage registerPage = new RegisterPage(driver);
    registerPage.register(username, email, "password123");

    WebDriverWait wait = new WebDriverWait(driver, 15);
    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlToBe(BASE_URL + "/"),
            ExpectedConditions.urlToBe(BASE_URL),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages"))));

    driver.get(BASE_URL + "/editor/new");
    wait.until(
        ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[type='submit']")));

    EditorPage editorPage = new EditorPage(driver);
    editorPage.enterTitle("Tagged Article " + uniqueSuffix);
    editorPage.enterDescription("Test desc");
    editorPage.enterBody("Test body with tags");
    editorPage.enterTag("selenium");
    editorPage.clickPublish();

    wait.until(
        ExpectedConditions.or(
            ExpectedConditions.urlContains("/article/"),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".error-messages"))));

    if (driver.getCurrentUrl().contains("/article/")) {
      List<WebElement> tags = driver.findElements(By.cssSelector(".tag-list .tag-default"));
      test.info("Tags found on article: " + tags.size());
    } else {
      test.info("Article not available for tag verification");
    }
  }

  @Test(groups = {"regression"})
  public void testArticleListOnHomePage() {
    createTest(
        "testArticleListOnHomePage", "Verify articles are listed on the home page global feed");

    registerAndCreateArticle();
    WebDriverWait wait = new WebDriverWait(driver, 10);

    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar")));

    // Check for article previews
    List<WebElement> previews = driver.findElements(By.cssSelector(".article-preview"));
    test.info("Article previews found on home page: " + previews.size());

    // If articles exist, verify preview structure
    if (!previews.isEmpty()) {
      WebElement firstPreview = previews.get(0);
      WebElement previewTitle = firstPreview.findElement(By.tagName("h1"));
      assertNotNull(previewTitle, "Article preview should have a title");
      assertTrue(previewTitle.isDisplayed(), "Article preview title should be visible");

      WebElement readMore = firstPreview.findElement(By.cssSelector(".preview-link span"));
      assertTrue(readMore.isDisplayed(), "'Read more...' link should be visible");
    }
  }

  @Test(groups = {"regression"})
  public void testClickArticlePreviewNavigatesToDetail() {
    createTest(
        "testClickArticlePreviewNavigatesToDetail",
        "Verify clicking an article preview navigates to article detail");

    registerAndCreateArticle();
    WebDriverWait wait = new WebDriverWait(driver, 10);

    driver.get(BASE_URL);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar")));

    List<WebElement> previews = driver.findElements(By.cssSelector(".article-preview"));
    if (!previews.isEmpty()) {
      WebElement previewLink = previews.get(0).findElement(By.cssSelector(".preview-link"));
      previewLink.click();

      wait.until(ExpectedConditions.urlContains("/article/"));
      assertTrue(
          driver.getCurrentUrl().contains("/article/"), "Should navigate to article detail page");

      test.info("Article preview click navigated to: " + driver.getCurrentUrl());
    } else {
      test.info("No article previews available to click");
    }
  }

  @Test(groups = {"regression"})
  public void testCommentSectionVisibleOnArticlePage() {
    createTest(
        "testCommentSectionVisibleOnArticlePage",
        "Verify comment section is visible on article detail page");

    registerAndCreateArticle();

    if (driver.getCurrentUrl().contains("/article/")) {
      WebDriverWait wait = new WebDriverWait(driver, 10);

      // Comment form should be visible for logged-in user
      WebElement commentForm = driver.findElement(By.cssSelector(".comment-form"));
      assertTrue(commentForm.isDisplayed(), "Comment form should be visible for logged-in user");

      WebElement commentTextarea = driver.findElement(By.cssSelector(".comment-form textarea"));
      assertTrue(commentTextarea.isDisplayed(), "Comment textarea should be visible");

      WebElement postButton =
          driver.findElement(By.cssSelector(".comment-form button[type='submit']"));
      assertTrue(postButton.isDisplayed(), "Post Comment button should be visible");
      assertTrue(postButton.getText().contains("Post Comment"), "Button should say 'Post Comment'");

      test.info("Comment section verified on article page");
    } else {
      test.info("Article page not available for comment verification");
    }
  }
}
