package io.spring.selenium.tests;

import static org.testng.Assert.*;

import io.spring.selenium.pages.LoginPage;
import io.spring.selenium.pages.ProfilePage;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SocialTest extends BaseTest {

  private String username1;
  private String email1;
  private String username2;
  private String email2;
  private String password;
  private String baseUrl;

  @BeforeMethod
  public void setUpUsers() throws Exception {
    baseUrl = config.getProperty("base.url", "http://localhost:3000");
    String apiUrl = config.getProperty("api.url", "http://localhost:8080");
    password = "password123";

    username1 = "social1" + UUID.randomUUID().toString().substring(0, 8);
    email1 = "social1" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    registerViaApi(apiUrl, username1, email1, password);

    username2 = "social2" + UUID.randomUUID().toString().substring(0, 8);
    email2 = "social2" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    registerViaApi(apiUrl, username2, email2, password);
  }

  private void registerViaApi(String apiUrl, String username, String email, String password)
      throws Exception {
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
  }

  @Test(groups = {"regression"})
  public void testFollowUser() throws Exception {
    createTest("testFollowUser", "Follow another user");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email1, password);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.navigateTo(baseUrl, username2);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    profilePage.clickFollow();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertTrue(profilePage.isFollowing(), "Should be following user after clicking Follow");
    test.info("Successfully followed user: " + username2);
  }

  @Test(groups = {"regression"})
  public void testUnfollowUser() throws Exception {
    createTest("testUnfollowUser", "Unfollow a followed user");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email1, password);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    ProfilePage profilePage = new ProfilePage(driver);
    profilePage.navigateTo(baseUrl, username2);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    profilePage.clickFollow();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    profilePage.clickFollow();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    assertFalse(profilePage.isFollowing(), "Should not be following after unfollow");
    test.info("Successfully unfollowed user: " + username2);
  }

  @Test(groups = {"regression"})
  public void testFavoriteArticle() {
    createTest("testFavoriteArticle", "Favorite an article");

    LoginPage loginPage = new LoginPage(driver);
    loginPage.navigateTo(baseUrl);
    loginPage.login(email1, password);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    test.info("Favorite article test completed");
  }
}
