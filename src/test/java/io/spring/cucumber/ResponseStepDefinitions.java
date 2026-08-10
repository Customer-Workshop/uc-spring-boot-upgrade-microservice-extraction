package io.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import io.spring.core.article.Article;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Assertions on the last API response. */
public class ResponseStepDefinitions {

  private final ScenarioContext context;

  public ResponseStepDefinitions(ScenarioContext context) {
    this.context = context;
  }

  @Then("the response status should be {int}")
  public void theResponseStatusShouldBe(int status) {
    Response response = context.response();
    assertEquals(status, response.statusCode(), "unexpected body: " + response.asString());
  }

  @Then("the response article title should be {string}")
  public void theResponseArticleTitleShouldBe(String expected) {
    assertEquals(expected, context.response().path("article.title"));
  }

  @Then("the response article description should be {string}")
  public void theResponseArticleDescriptionShouldBe(String expected) {
    assertEquals(expected, context.response().path("article.description"));
  }

  @Then("the response article body should be {string}")
  public void theResponseArticleBodyShouldBe(String expected) {
    assertEquals(expected, context.response().path("article.body"));
  }

  @Then("the response article author should be {string}")
  public void theResponseArticleAuthorShouldBe(String expected) {
    assertEquals(expected, context.response().path("article.author.username"));
  }

  @Then("the response article slug should be derived from {string}")
  public void theResponseArticleSlugShouldBeDerivedFrom(String title) {
    assertEquals(Article.toSlug(title), context.response().path("article.slug"));
  }

  @Then("the response article tags should be {string}")
  public void theResponseArticleTagsShouldBe(String tags) {
    List<String> expected =
        Arrays.stream(tags.split(",")).map(String::trim).sorted().collect(Collectors.toList());
    List<String> actual = context.response().path("article.tagList");
    assertEquals(expected, actual.stream().sorted().collect(Collectors.toList()));
  }

  @Then("the response article should be favorited")
  public void theResponseArticleShouldBeFavorited() {
    boolean favorited = context.response().path("article.favorited");
    assertTrue(favorited, "article is not favorited");
  }

  @Then("the response article should not be favorited")
  public void theResponseArticleShouldNotBeFavorited() {
    boolean favorited = context.response().path("article.favorited");
    assertFalse(favorited, "article is favorited");
  }

  @Then("the response article favorites count should be {int}")
  public void theResponseArticleFavoritesCountShouldBe(int count) {
    assertEquals(count, (int) context.response().path("article.favoritesCount"));
  }

  @Then("the response should contain {int} article(s)")
  public void theResponseShouldContainArticles(int count) {
    List<?> articles = context.response().path("articles");
    assertEquals(count, articles.size(), "unexpected body: " + context.response().asString());
  }

  @Then("the response articles count should be {int}")
  public void theResponseArticlesCountShouldBe(int count) {
    assertEquals(count, (int) context.response().path("articlesCount"));
  }

  @Then("the response should contain the article {string}")
  public void theResponseShouldContainTheArticle(String title) {
    assertTrue(titles().contains(title), "got " + titles());
  }

  @Then("the response should not contain the article {string}")
  public void theResponseShouldNotContainTheArticle(String title) {
    assertFalse(titles().contains(title), "got " + titles());
  }

  @Then("the article {string} in the response should have {int} favorite(s)")
  public void theArticleInTheResponseShouldHaveFavorites(String title, int count) {
    List<Integer> counts =
        context.response().path("articles.findAll { it.title == '" + title + "' }.favoritesCount");
    assertEquals(1, counts.size(), "article not found in the response");
    assertEquals(count, (int) counts.get(0));
  }

  @Then("the response should report {string} for the field {string}")
  public void theResponseShouldReportForTheField(String message, String field) {
    List<String> messages = context.response().path("errors." + field);
    assertTrue(
        messages != null && messages.contains(message),
        "unexpected body: " + context.response().asString());
  }

  private List<String> titles() {
    return context.response().path("articles.title");
  }
}
