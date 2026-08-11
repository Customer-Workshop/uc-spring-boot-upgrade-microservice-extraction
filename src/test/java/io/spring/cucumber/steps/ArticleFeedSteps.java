package io.spring.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.spring.cucumber.support.ApiContext;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class ArticleFeedSteps {

  @Autowired private ApiContext context;

  @When("I request the global feed")
  public void requestGlobalFeed() {
    context.setLastResponse(context.request().get("/articles").andReturn());
  }

  @When("I request the global feed filtered by author {string}")
  public void requestGlobalFeedByAuthor(String author) {
    context.setLastResponse(
        context.request().queryParam("author", author).get("/articles").andReturn());
  }

  @When("I request the global feed filtered by tag {string}")
  public void requestGlobalFeedByTag(String tag) {
    context.setLastResponse(context.request().queryParam("tag", tag).get("/articles").andReturn());
  }

  @When("I request the global feed filtered by favorited by {string}")
  public void requestGlobalFeedByFavoritedBy(String username) {
    context.setLastResponse(
        context.request().queryParam("favorited", username).get("/articles").andReturn());
  }

  @When("I request the global feed with limit {int} and offset {int}")
  public void requestGlobalFeedPaginated(int limit, int offset) {
    context.setLastResponse(
        context
            .request()
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .get("/articles")
            .andReturn());
  }

  @When("I request my user feed")
  public void requestUserFeed() {
    context.setLastResponse(context.request().get("/articles/feed").andReturn());
  }

  @When("I request my user feed with limit {int} and offset {int}")
  public void requestUserFeedPaginated(int limit, int offset) {
    context.setLastResponse(
        context
            .request()
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .get("/articles/feed")
            .andReturn());
  }

  @When("I remember the returned article titles as {string}")
  public void rememberTitles(String key) {
    context.rememberTitles(key, titles());
  }

  @Then("the feed should contain {int} articles")
  public void assertFeedSize(int expected) {
    assertEquals(expected, titles().size(), "unexpected articles: " + titles());
  }

  @Then("the articles count should be {int}")
  public void assertArticlesCount(int expected) {
    assertEquals(expected, context.getLastResponse().jsonPath().getInt("articlesCount"));
  }

  @Then("the feed should contain the article {string}")
  public void assertFeedContains(String title) {
    assertTrue(titles().contains(title), "expected " + title + " in " + titles());
  }

  @Then("every article in the feed should be authored by {string}")
  public void assertFeedAuthors(String author) {
    List<String> authors = context.getLastResponse().jsonPath().getList("articles.author.username");
    assertTrue(!authors.isEmpty(), "the feed is empty");
    assertTrue(
        authors.stream().allMatch(author::equals),
        "expected only articles by " + author + " but got " + authors);
  }

  @Then("every article in the feed should be tagged {string}")
  public void assertFeedTags(String tag) {
    List<List<String>> tagLists = context.getLastResponse().jsonPath().getList("articles.tagList");
    assertTrue(!tagLists.isEmpty(), "the feed is empty");
    assertTrue(
        tagLists.stream().allMatch(tags -> tags.contains(tag)),
        "expected every article to be tagged " + tag + " but got " + tagLists);
  }

  @Then("the feed article {string} should have a favorites count of {int}")
  public void assertFeedArticleFavoritesCount(String title, int expected) {
    Integer count =
        context
            .getLastResponse()
            .jsonPath()
            .getInt("articles.find { it.title == '" + title + "' }.favoritesCount");
    assertEquals(expected, count);
  }

  @Then("{string} and {string} should not share any article")
  public void assertPagesDoNotOverlap(String firstKey, String secondKey) {
    List<String> first = context.rememberedTitles(firstKey);
    List<String> second = context.rememberedTitles(secondKey);
    assertTrue(Collections.disjoint(first, second), first + " overlaps with " + second);
  }

  private List<String> titles() {
    return context.getLastResponse().jsonPath().getList("articles.title");
  }
}
