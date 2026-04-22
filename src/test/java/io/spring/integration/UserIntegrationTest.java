package io.spring.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class UserIntegrationTest extends BaseIntegrationTest {

  private String uniqueEmail() {
    return "user" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
  }

  private String uniqueUsername() {
    return "user" + UUID.randomUUID().toString().substring(0, 8);
  }

  @Test
  public void testRegistration() {
    String email = uniqueEmail();
    String username = uniqueUsername();

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", "password123");

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/users")
        .then()
        .statusCode(201)
        .body("user.token", notNullValue())
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username));
  }

  @Test
  public void testLogin() {
    String email = uniqueEmail();
    String username = uniqueUsername();
    String password = "password123";

    registerAndGetToken(email, username, password);

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/users/login")
        .then()
        .statusCode(200)
        .body("user.token", notNullValue())
        .body("user.email", equalTo(email));
  }

  @Test
  public void testGetCurrentUser() {
    String email = uniqueEmail();
    String username = uniqueUsername();
    String token = registerAndGetToken(email, username, "password123");

    given()
        .header("Authorization", "Token " + token)
        .when()
        .get("/user")
        .then()
        .statusCode(200)
        .body("user.email", equalTo(email))
        .body("user.username", equalTo(username));
  }

  @Test
  public void testUpdateUser() {
    String email = uniqueEmail();
    String username = uniqueUsername();
    String token = registerAndGetToken(email, username, "password123");

    Map<String, Object> user = new HashMap<>();
    user.put("bio", "Updated bio");

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .header("Authorization", "Token " + token)
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .put("/user")
        .then()
        .statusCode(200)
        .body("user.bio", equalTo("Updated bio"));
  }

  @Test
  public void testBlankUsernameValidation() {
    String email = uniqueEmail();

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", "");
    user.put("password", "password123");

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/users")
        .then()
        .statusCode(422);
  }

  @Test
  public void testDuplicateEmailValidation() {
    String email = uniqueEmail();
    String username1 = uniqueUsername();
    String username2 = uniqueUsername();

    registerAndGetToken(email, username1, "password123");

    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username2);
    user.put("password", "password123");

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    given()
        .contentType(ContentType.JSON)
        .body(body)
        .when()
        .post("/users")
        .then()
        .statusCode(422);
  }
}
