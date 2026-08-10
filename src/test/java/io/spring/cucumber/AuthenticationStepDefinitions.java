package io.spring.cucumber;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.DataTableType;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Registration, login and follow relationships. */
public class AuthenticationStepDefinitions {

  private final ApiClient apiClient;
  private final ScenarioContext context;

  public AuthenticationStepDefinitions(ApiClient apiClient, ScenarioContext context) {
    this.apiClient = apiClient;
    this.context = context;
  }

  @DataTableType
  public UserRow userRow(Map<String, String> entry) {
    return new UserRow(entry.get("username"), entry.get("email"), entry.get("password"));
  }

  @Given("the following users are registered:")
  public void theFollowingUsersAreRegistered(List<UserRow> users) {
    users.forEach(user -> register(user.username, user.email, user.password));
  }

  @Given("{string} is logged in")
  public void isLoggedIn(String username) {
    Response response = apiClient.post("/users/login", wrap("user", credentials(username)), null);
    assertEquals(200, response.statusCode(), "login failed: " + response.asString());
    context.registerUser(
        username,
        context.emailOf(username),
        context.passwordOf(username),
        response.path("user.token"));
    context.login(username);
  }

  @Given("no one is logged in")
  public void noOneIsLoggedIn() {
    context.logout();
  }

  @Given("{string} follows {string}")
  public void follows(String follower, String followed) {
    Response response =
        apiClient.post("/profiles/" + followed + "/follow", null, context.tokenOf(follower));
    assertEquals(200, response.statusCode(), "follow failed: " + response.asString());
  }

  private void register(String username, String email, String password) {
    Map<String, Object> user = new HashMap<>();
    user.put("username", username);
    user.put("email", email);
    user.put("password", password);

    Response response = apiClient.post("/users", wrap("user", user), null);
    assertEquals(201, response.statusCode(), "registration failed: " + response.asString());
    context.registerUser(username, email, password, response.path("user.token"));
  }

  private Map<String, Object> credentials(String username) {
    Map<String, Object> credentials = new HashMap<>();
    credentials.put("email", context.emailOf(username));
    credentials.put("password", context.passwordOf(username));
    return credentials;
  }

  private Map<String, Object> wrap(String root, Map<String, Object> body) {
    Map<String, Object> wrapped = new HashMap<>();
    wrapped.put(root, body);
    return wrapped;
  }

  static class UserRow {
    private final String username;
    private final String email;
    private final String password;

    UserRow(String username, String email, String password) {
      this.username = username;
      this.email = email;
      this.password = password;
    }
  }
}
