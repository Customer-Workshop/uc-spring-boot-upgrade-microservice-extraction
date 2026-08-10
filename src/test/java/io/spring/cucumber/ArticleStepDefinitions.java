package io.spring.cucumber;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.spring.core.article.Article;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Article creation, listing and favoriting. */
public class ArticleStepDefinitions {

  /** Marker for a value a scenario wants to send as an empty string, as Gherkin cells are null. */
  private static final String EMPTY = "[empty]";

  private final ApiClient apiClient;
  private final ScenarioContext context;

  public ArticleStepDefinitions(ApiClient apiClient, ScenarioContext context) {
    this.apiClient = apiClient;
    this.context = context;
  }

  @Given("{string} has published the following articles:")
  public void hasPublishedTheFollowingArticles(
      String username, List<Map<String, String>> articles) {
    articles.forEach(
        article -> {
          Response response =
              apiClient.post("/articles", articleBody(article), context.tokenOf(username));
          assertEquals(
              200, response.statusCode(), "article creation failed: " + response.asString());
        });
  }

  @Given("{string} has favorited the article {string}")
  public void hasFavoritedTheArticle(String username, String title) {
    Response response =
        apiClient.post(
            "/articles/" + Article.toSlug(title) + "/favorite", null, context.tokenOf(username));
    assertEquals(200, response.statusCode(), "favorite failed: " + response.asString());
  }

  @When("I create an article with:")
  public void iCreateAnArticleWith(Map<String, String> article) {
    context.setResponse(apiClient.post("/articles", articleBody(article), context.currentToken()));
  }

  @When("I request the article {string}")
  public void iRequestTheArticle(String title) {
    context.setResponse(
        apiClient.get("/articles/" + Article.toSlug(title), emptyMap(), context.currentToken()));
  }

  @When("I request the global feed")
  public void iRequestTheGlobalFeed() {
    iRequestTheGlobalFeedWith(emptyMap());
  }

  @When("I request the global feed with:")
  public void iRequestTheGlobalFeedWith(Map<String, String> queryParams) {
    context.setResponse(apiClient.get("/articles", queryParams, context.currentToken()));
  }

  @When("I request my feed")
  public void iRequestMyFeed() {
    iRequestMyFeedWith(emptyMap());
  }

  @When("I request my feed with:")
  public void iRequestMyFeedWith(Map<String, String> queryParams) {
    context.setResponse(apiClient.get("/articles/feed", queryParams, context.currentToken()));
  }

  @When("I favorite the article {string}")
  public void iFavoriteTheArticle(String title) {
    context.setResponse(
        apiClient.post(
            "/articles/" + Article.toSlug(title) + "/favorite", null, context.currentToken()));
  }

  @When("I unfavorite the article {string}")
  public void iUnfavoriteTheArticle(String title) {
    context.setResponse(
        apiClient.delete(
            "/articles/" + Article.toSlug(title) + "/favorite", context.currentToken()));
  }

  /**
   * Builds the request payload from the columns present in the table, so a scenario can leave out a
   * field to exercise the validation. `tagList` is always sent because the API does not accept a
   * null tag list.
   */
  private Map<String, Object> articleBody(Map<String, String> article) {
    Map<String, Object> payload = new HashMap<>();
    Arrays.asList("title", "description", "body")
        .forEach(
            field -> {
              if (article.containsKey(field)) {
                payload.put(field, EMPTY.equals(article.get(field)) ? "" : article.get(field));
              }
            });
    payload.put("tagList", tagList(article.get("tags")));

    Map<String, Object> wrapped = new HashMap<>();
    wrapped.put("article", payload);
    return wrapped;
  }

  private List<String> tagList(String tags) {
    if (tags == null || tags.trim().isEmpty()) {
      return new ArrayList<>();
    }
    return Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList());
  }
}
