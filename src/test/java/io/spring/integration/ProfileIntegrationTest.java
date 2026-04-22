package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProfileIntegrationTest extends BaseIntegrationTest {

  private String token1;
  private String username1;
  private String token2;
  private String username2;

  @BeforeEach
  public void setUp() {
    String email1 = "profile1" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    username1 = "profuser1" + UUID.randomUUID().toString().substring(0, 8);
    token1 = registerAndGetToken(email1, username1, "password123");

    String email2 = "profile2" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    username2 = "profuser2" + UUID.randomUUID().toString().substring(0, 8);
    token2 = registerAndGetToken(email2, username2, "password123");
  }

  @Test
  public void testGetProfile() {
    given()
        .header("Authorization", "Token " + token1)
        .when()
        .get("/profiles/" + username2)
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(username2));
  }

  @Test
  public void testFollowUser() {
    given()
        .header("Authorization", "Token " + token1)
        .when()
        .post("/profiles/" + username2 + "/follow")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(username2))
        .body("profile.following", is(true));
  }

  @Test
  public void testUnfollowUser() {
    given()
        .header("Authorization", "Token " + token1)
        .when()
        .post("/profiles/" + username2 + "/follow");

    given()
        .header("Authorization", "Token " + token1)
        .when()
        .delete("/profiles/" + username2 + "/follow")
        .then()
        .statusCode(200)
        .body("profile.username", equalTo(username2))
        .body("profile.following", is(false));
  }
}
