package io.spring.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.response.Response;
import io.spring.cucumber.support.ApiContext;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/** Steps shared by all the Articles API features: users, authentication and article fixtures. */
public class CommonSteps {

  @Autowired private ApiContext context;

  @Given("a registered user {string} with email {string} and password {string}")
  public void registerUser(String username, String email, String password) {
    Map<String, Object> user = new LinkedHashMap<>();
    user.put("username", username);
    user.put("email", email);
    user.put("password", password);

    Response response =
        context.anonymousRequest().body(context.wrap("user", user)).post("/users").andReturn();

    assertEquals(
        201, response.statusCode(), "failed to register " + username + ": " + response.asString());
    context.rememberToken(username, response.jsonPath().getString("user.token"));
  }

  @Given("I am logged in as {string}")
  public void loginAs(String username) {
    context.setCurrentUsername(username);
  }

  @Given("I am not logged in")
  public void logout() {
    context.setCurrentUsername(null);
  }

  @Given("an article titled {string} exists authored by {string}")
  public void articleExists(String title, String author) {
    createArticleAs(author, title, "description of " + title, "body of " + title, List.of());
  }

  @Given("the following articles exist:")
  public void articlesExist(DataTable table) {
    for (Map<String, String> row : table.asMaps()) {
      String tags = row.getOrDefault("tags", "");
      createArticleAs(
          row.get("author"),
          row.get("title"),
          row.getOrDefault("description", "description"),
          row.getOrDefault("body", "body"),
          splitTags(tags));
    }
  }

  @Given("I follow {string}")
  public void followUser(String username) {
    Response response = context.request().post("/profiles/{username}/follow", username).andReturn();
    assertEquals(
        200, response.statusCode(), "failed to follow " + username + ": " + response.asString());
  }

  @Then("the response status should be {int}")
  public void assertStatus(int expected) {
    Response response = context.getLastResponse();
    assertEquals(expected, response.statusCode(), "unexpected response: " + response.asString());
  }

  /** Creates an article through the API on behalf of the given author. */
  private void createArticleAs(
      String author, String title, String description, String body, List<String> tags) {
    Map<String, Object> article = new LinkedHashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", tags);

    Response response =
        context
            .anonymousRequest()
            .header("Authorization", "Token " + context.tokenOf(author))
            .body(context.wrap("article", article))
            .post("/articles")
            .andReturn();

    assertEquals(
        200,
        response.statusCode(),
        "failed to create article " + title + ": " + response.asString());
    context.rememberSlug(title, response.jsonPath().getString("article.slug"));
  }

  private List<String> splitTags(String tags) {
    if (tags == null || tags.trim().isEmpty()) {
      return List.of();
    }
    return Arrays.stream(tags.split(","))
        .map(String::trim)
        .filter(tag -> !tag.isEmpty())
        .collect(Collectors.toList());
  }
}
