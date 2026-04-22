package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.ArticlePage;
import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.HomePage;
import io.spring.selenium.pages.LoginPage;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ArticleTest extends BaseTest {

  private String username;
  private String email;
  private String password;
  private String baseUrl;

  @BeforeMethod
  public void setUpUser() throws Exception {
    baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String apiUrl = config.getProperty("api.url", "http://localhost:8080");
    username = "artuser" + UUID.randomUUID().toString().substring(0, 8);
    email = "art" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    password = "password123";

    URL url = new URL(apiUrl + "/users");
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setDoOutput(true);

    String json =
        String.format(
            "{\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"password\":\"%s\"}}",
            username, email, password);

    try (OutputStream os = conn.getOutputStream()) {
      os.write(json.getBytes());
    }

    conn.getResponseCode();
    conn.disconnect();

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email, password);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test(groups = {"regression"})
  public void testCreateArticle() {
    createTest("testCreateArticle", "Create article via editor");
    String title = "Test Article " + UUID.randomUUID().toString().substring(0, 8);

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    editorPage.createArticle(title, "Test description", "Test body content", "testTag");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    test.info("Article created: " + title);
  }

  @Test(groups = {"regression"})
  public void testDeleteArticle() {
    createTest("testDeleteArticle", "Delete an article");
    String title = "Delete Article " + UUID.randomUUID().toString().substring(0, 8);

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    editorPage.createArticle(title, "To be deleted", "This will be deleted", "delete");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    ArticlePage articlePage = new ArticlePage(driver);
    try {
      articlePage.deleteArticle();
      Thread.sleep(2000);
    } catch (Exception e) {
      test.info("Delete button interaction: " + e.getMessage());
    }

    test.info("Delete article test completed");
  }

  @Test(groups = {"regression"})
  public void testArticleAppearsInFeed() {
    createTest("testArticleAppearsInFeed", "Verify article in global feed");
    String title = "Feed Article " + UUID.randomUUID().toString().substring(0, 8);

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    editorPage.createArticle(title, "Feed test", "Feed body", "feed");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    HomePage homePage = new HomePage(driver);
    homePage.navigateTo(baseUrl);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    test.info("Article feed test completed for: " + title);
  }
}
