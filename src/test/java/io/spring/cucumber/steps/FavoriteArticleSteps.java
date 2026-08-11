package io.spring.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.spring.cucumber.support.ApiContext;
import org.springframework.beans.factory.annotation.Autowired;

public class FavoriteArticleSteps {

  @Autowired private ApiContext context;

  @Given("I have favorited the article {string}")
  public void haveFavorited(String title) {
    Response response = favorite(context.slugOf(title));
    assertEquals(
        200, response.statusCode(), "failed to favorite " + title + ": " + response.asString());
  }

  @When("I favorite the article {string}")
  public void favoriteArticle(String title) {
    context.setLastResponse(favorite(context.slugOf(title)));
  }

  @When("I favorite the article with slug {string}")
  public void favoriteArticleBySlug(String slug) {
    context.setLastResponse(favorite(slug));
  }

  @When("I unfavorite the article {string}")
  public void unfavoriteArticle(String title) {
    context.setLastResponse(
        context.request().delete("/articles/{slug}/favorite", context.slugOf(title)).andReturn());
  }

  @When("I fetch the article {string}")
  public void fetchArticle(String title) {
    context.setLastResponse(
        context.request().get("/articles/{slug}", context.slugOf(title)).andReturn());
  }

  @Then("the article should be marked as favorited")
  public void assertFavorited() {
    assertTrue(favoritedFlag(), "expected the article to be favorited");
  }

  @Then("the article should not be marked as favorited")
  public void assertNotFavorited() {
    assertFalse(favoritedFlag(), "expected the article not to be favorited");
  }

  @Then("the article favorites count should be {int}")
  public void assertFavoritesCount(int expected) {
    assertEquals(expected, context.getLastResponse().jsonPath().getInt("article.favoritesCount"));
  }

  private Response favorite(String slug) {
    return context.request().post("/articles/{slug}/favorite", slug).andReturn();
  }

  private boolean favoritedFlag() {
    return context.getLastResponse().jsonPath().getBoolean("article.favorited");
  }
}
