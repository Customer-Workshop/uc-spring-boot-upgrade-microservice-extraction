package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonSteps {

  @Autowired private SharedState sharedState;

  @Given("a registered user exists")
  public void registeredUserExists() {
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    Map<String, Object> user = new HashMap<>();
    user.put("username", "testuser-" + uniqueId);
    user.put("email", "testuser-" + uniqueId + "@test.com");
    user.put("password", "password123");
    Map<String, Object> payload = new HashMap<>();
    payload.put("user", user);

    Response response =
        RestAssured.given().contentType(ContentType.JSON).body(payload).post("/users");

    String token = response.jsonPath().getString("user.token");
    sharedState.setAuthToken(token);
  }

  @Given("the user is authenticated")
  public void userIsAuthenticated() {
    // The token is already set during registration
  }

  @Then("the response status should be {int}")
  public void responseStatusShouldBe(int expectedStatus) {
    assertThat(sharedState.getLastResponse().getStatusCode(), equalTo(expectedStatus));
  }

  @Then("the response should contain article with title {string}")
  public void responseContainsArticleWithTitle(String title) {
    String actualTitle = sharedState.getLastResponse().jsonPath().getString("article.title");
    assertThat(actualTitle, equalTo(title));
  }

  @Then("the response should contain article with slug {string}")
  public void responseContainsArticleWithSlug(String slug) {
    String actualSlug = sharedState.getLastResponse().jsonPath().getString("article.slug");
    assertThat(actualSlug, equalTo(slug));
  }

  @Then("the response should contain article with description {string}")
  public void responseContainsArticleWithDescription(String description) {
    String actualDesc = sharedState.getLastResponse().jsonPath().getString("article.description");
    assertThat(actualDesc, equalTo(description));
  }

  @Then("the response should contain article with body {string}")
  public void responseContainsArticleWithBody(String body) {
    String actualBody = sharedState.getLastResponse().jsonPath().getString("article.body");
    assertThat(actualBody, equalTo(body));
  }

  @Then("the article should have tag {string}")
  public void articleShouldHaveTag(String tag) {
    List<String> tags = sharedState.getLastResponse().jsonPath().getList("article.tagList");
    assertThat(tags, hasItem(tag));
  }
}
