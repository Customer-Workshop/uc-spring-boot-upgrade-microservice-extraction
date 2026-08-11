package io.spring.cucumber.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import io.spring.cucumber.support.ApiContext;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

public class CreateArticleSteps {

  @Autowired private ApiContext context;

  @When("I create an article with title {string}, description {string} and body {string}")
  public void createArticle(String title, String description, String body) {
    postArticle(title, description, body, List.of());
  }

  @When(
      "I create an article with title {string}, description {string}, body {string} and tags"
          + " {string}")
  public void createArticleWithTags(String title, String description, String body, String tags) {
    postArticle(title, description, body, splitTags(tags));
  }

  @Then("the article title should be {string}")
  public void assertTitle(String expected) {
    assertEquals(expected, articleField("title"));
  }

  @Then("the article slug should be {string}")
  public void assertSlug(String expected) {
    assertEquals(expected, articleField("slug"));
  }

  @Then("the article description should be {string}")
  public void assertDescription(String expected) {
    assertEquals(expected, articleField("description"));
  }

  @Then("the article body should be {string}")
  public void assertBody(String expected) {
    assertEquals(expected, articleField("body"));
  }

  @Then("the article author should be {string}")
  public void assertAuthor(String expected) {
    assertEquals(expected, articleField("author.username"));
  }

  @Then("the article tag list should contain {string} and {string}")
  public void assertTagList(String first, String second) {
    List<String> tagList = context.getLastResponse().jsonPath().getList("article.tagList");
    assertTrue(tagList.contains(first), "expected tag " + first + " in " + tagList);
    assertTrue(tagList.contains(second), "expected tag " + second + " in " + tagList);
  }

  @Then("the article tag list should be empty")
  public void assertEmptyTagList() {
    List<String> tagList = context.getLastResponse().jsonPath().getList("article.tagList");
    assertTrue(tagList.isEmpty(), "expected no tags but got " + tagList);
  }

  @Then("the response should report the error {string} as {string}")
  public void assertFieldError(String field, String message) {
    List<String> messages = context.getLastResponse().jsonPath().getList("errors." + field);
    assertTrue(
        messages != null && messages.contains(message),
        "expected error '"
            + message
            + "' for field '"
            + field
            + "' but got "
            + context.getLastResponse().asString());
  }

  private void postArticle(String title, String description, String body, List<String> tags) {
    Map<String, Object> article = new LinkedHashMap<>();
    article.put("title", title);
    article.put("description", description);
    article.put("body", body);
    article.put("tagList", tags);

    Response response =
        context.request().body(context.wrap("article", article)).post("/articles").andReturn();
    context.setLastResponse(response);
    if (response.statusCode() == 200) {
      context.rememberSlug(title, response.jsonPath().getString("article.slug"));
    }
  }

  private String articleField(String path) {
    return context.getLastResponse().jsonPath().getString("article." + path);
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
