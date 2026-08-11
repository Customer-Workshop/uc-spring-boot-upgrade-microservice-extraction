package io.spring.cucumber.support;

import static io.restassured.RestAssured.given;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

/** Holds the state shared by the step definitions of a single scenario. */
public class ApiContext {

  @Value("${local.server.port}")
  private int port;

  private final Map<String, String> tokensByUsername = new HashMap<>();
  private final Map<String, String> slugsByTitle = new HashMap<>();
  private final Map<String, List<String>> rememberedTitles = new HashMap<>();
  private String currentUsername;
  private Response lastResponse;

  /** A request carrying the credentials of the currently logged in user, if any. */
  public RequestSpecification request() {
    RequestSpecification spec = anonymousRequest();
    String token = tokenOf(currentUsername);
    if (token != null) {
      spec = spec.header("Authorization", "Token " + token);
    }
    return spec;
  }

  public RequestSpecification anonymousRequest() {
    return given()
        .spec(new RequestSpecBuilder().setPort(port).setContentType(ContentType.JSON).build());
  }

  /** Wraps a payload in the root element expected by the API, e.g. {"article": {...}}. */
  public Map<String, Object> wrap(String root, Map<String, Object> payload) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put(root, payload);
    return body;
  }

  public void rememberToken(String username, String token) {
    tokensByUsername.put(username, token);
  }

  public String tokenOf(String username) {
    return username == null ? null : tokensByUsername.get(username);
  }

  public void rememberSlug(String title, String slug) {
    slugsByTitle.put(title, slug);
  }

  /** The slug created for the given title, falling back to the title itself when unknown. */
  public String slugOf(String title) {
    return slugsByTitle.getOrDefault(title, title);
  }

  public void rememberTitles(String key, List<String> titles) {
    rememberedTitles.put(key, titles);
  }

  public List<String> rememberedTitles(String key) {
    return rememberedTitles.get(key);
  }

  public String getCurrentUsername() {
    return currentUsername;
  }

  public void setCurrentUsername(String currentUsername) {
    this.currentUsername = currentUsername;
  }

  public Response getLastResponse() {
    return lastResponse;
  }

  public void setLastResponse(Response lastResponse) {
    this.lastResponse = lastResponse;
  }
}
