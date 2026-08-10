package io.spring.cucumber;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

/** State shared by the step definitions within a single scenario. */
public class ScenarioContext {

  private final Map<String, String> tokensByUsername = new HashMap<>();
  private final Map<String, String> passwordsByUsername = new HashMap<>();
  private final Map<String, String> emailsByUsername = new HashMap<>();
  private String currentUsername;
  private Response response;

  public void reset() {
    tokensByUsername.clear();
    passwordsByUsername.clear();
    emailsByUsername.clear();
    currentUsername = null;
    response = null;
  }

  public void registerUser(String username, String email, String password, String token) {
    emailsByUsername.put(username, email);
    passwordsByUsername.put(username, password);
    tokensByUsername.put(username, token);
  }

  public String passwordOf(String username) {
    return passwordsByUsername.get(username);
  }

  public String emailOf(String username) {
    return emailsByUsername.get(username);
  }

  public String tokenOf(String username) {
    String token = tokensByUsername.get(username);
    Assertions.assertNotNull(token, "no known user named " + username);
    return token;
  }

  public void login(String username) {
    tokenOf(username);
    currentUsername = username;
  }

  public void logout() {
    currentUsername = null;
  }

  public String currentUsername() {
    return currentUsername;
  }

  /** Token of the authenticated user, or null when the scenario runs anonymously. */
  public String currentToken() {
    return currentUsername == null ? null : tokenOf(currentUsername);
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public Response response() {
    Assertions.assertNotNull(response, "no request has been made yet");
    return response;
  }
}
