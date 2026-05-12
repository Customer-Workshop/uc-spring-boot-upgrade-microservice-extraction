package io.spring.cucumber.steps;

import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateArticleSteps {

  @Autowired private SharedState sharedState;

  @When("the user creates an article with title {string} description {string} and body {string}")
  public void createArticle(String title, String description, String body) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", Collections.emptyList());
    Map<String, Object> payload = new HashMap<>();
    payload.put("article", article);

    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getAuthToken())
            .body(payload)
            .post("/articles");

    sharedState.setLastResponse(response);
  }

  @When(
      "the user creates an article with title {string} description {string} body {string} and tags {string}")
  public void createArticleWithTags(String title, String description, String body, String tagsStr) {
    List<String> tags = Arrays.asList(tagsStr.split(","));
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", tags);
    Map<String, Object> payload = new HashMap<>();
    payload.put("article", article);

    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getAuthToken())
            .body(payload)
            .post("/articles");

    sharedState.setLastResponse(response);
  }
}
