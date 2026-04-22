package io.spring.integration;

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

  @LocalServerPort protected int port;

  @BeforeEach
  public void setUpBase() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  protected String registerAndGetToken(String email, String username, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("username", username);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    Response response =
        given().contentType(ContentType.JSON).body(body).when().post("/users").then().extract()
            .response();

    return response.jsonPath().getString("user.token");
  }

  protected String loginAndGetToken(String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("email", email);
    user.put("password", password);

    Map<String, Object> body = new HashMap<>();
    body.put("user", user);

    Response response =
        given()
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post("/users/login")
            .then()
            .extract()
            .response();

    return response.jsonPath().getString("user.token");
  }
}
