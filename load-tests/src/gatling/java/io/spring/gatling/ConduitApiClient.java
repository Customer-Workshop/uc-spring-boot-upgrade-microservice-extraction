package io.spring.gatling;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Plain JDK HTTP client used for simulation setup work (authentication, fixture creation, JVM warm
 * up). Setup traffic must not be measured, so it deliberately does not go through Gatling.
 */
final class ConduitApiClient {

  private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\":\"([^\"]+)\"");
  private static final Pattern SLUG_PATTERN = Pattern.compile("\"slug\":\"([^\"]+)\"");

  private final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
  private final String baseUrl;

  ConduitApiClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  String login(String email, String password) {
    String body =
        String.format("{\"user\":{\"email\":\"%s\",\"password\":\"%s\"}}", email, password);
    String response =
        send(
            request("/users/login")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build());
    Matcher matcher = TOKEN_PATTERN.matcher(response);
    if (!matcher.find()) {
      throw new IllegalStateException("No JWT token in login response: " + response);
    }
    return matcher.group(1);
  }

  /** Slugs of the articles that already exist, used as read fixtures. */
  List<String> existingSlugs(String token, int limit) {
    String response =
        send(
            request("/articles?limit=" + limit)
                .header("Authorization", "Token " + token)
                .GET()
                .build());
    List<String> slugs = new ArrayList<>();
    Matcher matcher = SLUG_PATTERN.matcher(response);
    while (matcher.find()) {
      slugs.add(matcher.group(1));
    }
    if (slugs.isEmpty()) {
      throw new IllegalStateException(
          "No articles found at " + baseUrl + ", is the seed data loaded?");
    }
    return slugs;
  }

  /** Creates throwaway articles so the delete branch always has its own targets. */
  List<String> createDisposableArticles(String token, int count) {
    List<String> slugs = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      String title = "loadtest disposable " + UUID.randomUUID();
      String body =
          String.format(
              "{\"article\":{\"title\":\"%s\",\"description\":\"load test fixture\",\"body\":\"load test fixture\",\"tagList\":[\"loadtest\"]}}",
              title);
      String response =
          send(
              request("/articles")
                  .header("Authorization", "Token " + token)
                  .header("Content-Type", "application/json")
                  .POST(HttpRequest.BodyPublishers.ofString(body))
                  .build());
      Matcher matcher = SLUG_PATTERN.matcher(response);
      if (!matcher.find()) {
        throw new IllegalStateException("Could not create fixture article: " + response);
      }
      slugs.add(matcher.group(1));
    }
    return slugs;
  }

  /** Drives some traffic so JIT compilation and connection pools are not measured as latency. */
  void warmUp(String token, String slug, int requests) {
    for (int i = 0; i < requests; i++) {
      send(request("/articles?limit=20").header("Authorization", "Token " + token).GET().build());
      send(request("/articles/" + slug).header("Authorization", "Token " + token).GET().build());
    }
  }

  private HttpRequest.Builder request(String path) {
    return HttpRequest.newBuilder(URI.create(baseUrl + path))
        .timeout(Duration.ofSeconds(30))
        .header("Accept", "application/json");
  }

  private String send(HttpRequest request) {
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        throw new IllegalStateException(
            "Setup request " + request.uri() + " failed with " + response.statusCode());
      }
      return response.body();
    } catch (IOException e) {
      throw new IllegalStateException("Setup request " + request.uri() + " failed", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Setup request " + request.uri() + " interrupted", e);
    }
  }
}
