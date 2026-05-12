package io.spring.cucumber.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

public class ArticleFeedSteps {

  @Autowired private SharedState sharedState;

  @Given("articles exist in the system")
  public void articlesExistInTheSystem() {
    // The seed data (V2 migration) creates articles when running without test profile.
    // For test profile with in-memory DB, we create articles via the API.
    // First, register a helper user and create a few articles.
    String uniqueId = UUID.randomUUID().toString().substring(0, 8);
    String helperToken =
        registerAndLogin("feedhelper-" + uniqueId, "feed-" + uniqueId + "@helper.com");

    createArticleViaApi(
        helperToken, "Feed Article One " + uniqueId, "Desc one", "Body one", "java");
    createArticleViaApi(
        helperToken, "Feed Article Two " + uniqueId, "Desc two", "Body two", "spring");
    createArticleViaApi(
        helperToken, "Feed Article Three " + uniqueId, "Desc three", "Body three", "java");
  }

  @Given("the user follows another author who has articles")
  public void userFollowsAuthorWithArticles() {
    String authorUsername = "author-" + UUID.randomUUID();
    String authorToken =
        registerAndLogin(authorUsername, "author-" + UUID.randomUUID() + "@test.com");

    createArticleViaApi(
        authorToken, "Followed Author Article " + UUID.randomUUID(), "Desc", "Body", "test");

    // Follow the author
    RestAssured.given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + sharedState.getAuthToken())
        .post("/profiles/" + authorUsername + "/follow");
  }

  @Given("a fresh user exists with no follows")
  public void freshUserExistsWithNoFollows() {
    String freshToken =
        registerAndLogin(
            "freshuser-" + UUID.randomUUID(), "fresh-" + UUID.randomUUID() + "@test.com");
    sharedState.setFreshUserAuthToken(freshToken);
  }

  @Given("the fresh user is authenticated")
  public void freshUserIsAuthenticated() {
    // Already authenticated during registration
  }

  @When("a user requests the global article feed")
  public void requestGlobalFeed() {
    Response response = RestAssured.given().contentType(ContentType.JSON).get("/articles");
    sharedState.setLastResponse(response);
  }

  @When("the user requests their personal feed")
  public void requestPersonalFeed() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getAuthToken())
            .get("/articles/feed");
    sharedState.setLastResponse(response);
  }

  @When("the fresh user requests their personal feed")
  public void freshUserRequestsPersonalFeed() {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Token " + sharedState.getFreshUserAuthToken())
            .get("/articles/feed");
    sharedState.setLastResponse(response);
  }

  @When("a user requests articles filtered by tag {string}")
  public void requestArticlesFilteredByTag(String tag) {
    Response response =
        RestAssured.given().contentType(ContentType.JSON).queryParam("tag", tag).get("/articles");
    sharedState.setLastResponse(response);
  }

  @When("a user requests articles with limit {int} and offset {int}")
  public void requestArticlesWithPagination(int limit, int offset) {
    Response response =
        RestAssured.given()
            .contentType(ContentType.JSON)
            .queryParam("limit", limit)
            .queryParam("offset", offset)
            .get("/articles");

    // If this is the first page request, store it for later comparison
    if (sharedState.getFirstPageResponse() == null) {
      sharedState.setFirstPageResponse(response);
    }
    sharedState.setLastResponse(response);
  }

  @Then("the response should contain a list of articles")
  public void responseContainsArticleList() {
    List<?> articles = sharedState.getLastResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
    assertThat(articles.size(), greaterThan(0));
  }

  @Then("the response should contain an articles count")
  public void responseContainsArticlesCount() {
    int count = sharedState.getLastResponse().jsonPath().getInt("articlesCount");
    assertThat(count, greaterThan(0));
  }

  @Then("the response should contain articles from followed authors")
  public void responseContainsArticlesFromFollowedAuthors() {
    List<?> articles = sharedState.getLastResponse().jsonPath().getList("articles");
    assertThat(articles, notNullValue());
    assertThat(articles.size(), greaterThan(0));
  }

  @Then("the feed should contain {int} articles")
  public void feedContainsArticles(int expectedCount) {
    int count = sharedState.getLastResponse().jsonPath().getInt("articlesCount");
    assertThat(count, org.hamcrest.Matchers.equalTo(expectedCount));
  }

  @Then("all returned articles should have tag {string}")
  public void allArticlesHaveTag(String tag) {
    List<List<String>> tagLists =
        sharedState.getLastResponse().jsonPath().getList("articles.tagList");
    for (List<String> tags : tagLists) {
      assertThat(tags, hasItem(tag));
    }
  }

  @Then("the response should contain at most {int} articles")
  public void responseContainsAtMostArticles(int maxCount) {
    List<?> articles = sharedState.getLastResponse().jsonPath().getList("articles");
    assertThat(articles.size(), lessThanOrEqualTo(maxCount));
  }

  @Then("the two pages should have different articles")
  public void twoPagesHaveDifferentArticles() {
    List<String> firstPageSlugs =
        sharedState.getFirstPageResponse().jsonPath().getList("articles.slug");
    List<String> secondPageSlugs =
        sharedState.getLastResponse().jsonPath().getList("articles.slug");

    boolean allDifferent = true;
    for (String slug : secondPageSlugs) {
      if (firstPageSlugs.contains(slug)) {
        allDifferent = false;
        break;
      }
    }
    assertThat("Second page should have different articles than first page", allDifferent);
  }

  private String registerAndLogin(String username, String email) {
    Map<String, Object> user = new HashMap<>();
    user.put("username", username);
    user.put("email", email);
    user.put("password", "password123");
    Map<String, Object> payload = new HashMap<>();
    payload.put("user", user);

    Response response =
        RestAssured.given().contentType(ContentType.JSON).body(payload).post("/users");

    return response.jsonPath().getString("user.token");
  }

  private void createArticleViaApi(
      String token, String title, String description, String body, String tag) {
    Map<String, Object> article = new HashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    if (tag != null) {
      article.put("tagList", java.util.Collections.singletonList(tag));
    } else {
      article.put("tagList", java.util.Collections.emptyList());
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("article", article);

    RestAssured.given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Token " + token)
        .body(payload)
        .post("/articles");
  }
}
