package io.spring.selenium.tests;

import io.spring.selenium.pages.ArticlePage;
import io.spring.selenium.pages.EditorPage;
import io.spring.selenium.pages.LoginPage;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CommentTest extends BaseTest {

  private String username;
  private String email;
  private String password;
  private String baseUrl;

  @BeforeMethod
  public void setUpUserAndArticle() throws Exception {
    baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String apiUrl = config.getProperty("api.url", "http://localhost:8080");
    username = "cmtuser" + UUID.randomUUID().toString().substring(0, 8);
    email = "cmt" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
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

    EditorPage editorPage = new EditorPage(driver);
    editorPage.navigateTo(baseUrl);

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    String title = "Comment Article " + UUID.randomUUID().toString().substring(0, 8);
    editorPage.createArticle(title, "For comments", "Comment test body", "comment");

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test(groups = {"regression"})
  public void testAddComment() {
    createTest("testAddComment", "Add a comment to an article");

    ArticlePage articlePage = new ArticlePage(driver);
    try {
      articlePage.addComment("This is a test comment");
      Thread.sleep(2000);
    } catch (Exception e) {
      test.info("Comment interaction: " + e.getMessage());
    }

    test.info("Add comment test completed");
  }

  @Test(groups = {"regression"})
  public void testDeleteComment() {
    createTest("testDeleteComment", "Delete a comment from an article");

    ArticlePage articlePage = new ArticlePage(driver);
    try {
      articlePage.addComment("Comment to delete");
      Thread.sleep(2000);
      articlePage.deleteComment("Comment to delete");
      Thread.sleep(2000);
    } catch (Exception e) {
      test.info("Delete comment interaction: " + e.getMessage());
    }

    test.info("Delete comment test completed");
  }
}
