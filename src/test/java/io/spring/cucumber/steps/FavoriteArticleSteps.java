package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public class FavoriteArticleSteps {

  @Autowired private SharedState sharedState;

  @Given("an article exists in the system")
  public void articleExistsInTheSystem() {
    Map<String, Object> article = new HashMap<>();
    article.put("title", "Favoritable Article " + UUID.randomUUID());
    article.put("description", "An article to favorite");
    article.put("body", "Body of the favoritable article");
    article.put("tagList", Collections.emptyList());
    Map<String, Object> payload = new HashMap<>();
    payload.put("article", article);

    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getAuthToken())
            .body(payload)
            .post("/articles");

    String slug = response.jsonPath().getString("article.slug");
    sharedState.setArticleSlug(slug);
  }

  @Given("the user has favorited the article")
  public void userHasFavoritedArticle() {
    RestAssured.given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + sharedState.getAuthToken())
        .post("/articles/" + sharedState.getArticleSlug() + "/favorite");
  }

  @Given("the initial favorites count is recorded")
  public void recordInitialFavoritesCount() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .get("/articles/" + sharedState.getArticleSlug());

    int count = response.jsonPath().getInt("article.favoritesCount");
    sharedState.setInitialFavoritesCount(count);
  }

  @When("the user favorites the article")
  public void favoriteArticle() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getAuthToken())
            .post("/articles/" + sharedState.getArticleSlug() + "/favorite");
    sharedState.setLastResponse(response);
  }

  @When("the user unfavorites the article")
  public void unfavoriteArticle() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getAuthToken())
            .delete("/articles/" + sharedState.getArticleSlug() + "/favorite");
    sharedState.setLastResponse(response);
  }

  @Then("the article should be marked as favorited")
  public void articleIsMarkedAsFavorited() {
    boolean favorited = sharedState.getLastResponse().jsonPath().getBoolean("article.favorited");
    assertThat(favorited, equalTo(true));
  }

  @Then("the article favorites count should be at least {int}")
  public void articleFavoritesCountAtLeast(int minCount) {
    int count = sharedState.getLastResponse().jsonPath().getInt("article.favoritesCount");
    assertThat(count, greaterThanOrEqualTo(minCount));
  }

  @Then("the article should not be marked as favorited")
  public void articleIsNotMarkedAsFavorited() {
    boolean favorited = sharedState.getLastResponse().jsonPath().getBoolean("article.favorited");
    assertThat(favorited, equalTo(false));
  }

  @Then("the article favorites count should be greater than the initial count")
  public void favoritesCountGreaterThanInitial() {
    int currentCount = sharedState.getLastResponse().jsonPath().getInt("article.favoritesCount");
    assertThat(currentCount, greaterThan(sharedState.getInitialFavoritesCount()));
  }
}
