package io.spring.comments;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies the comments microservice persists comments in its own database and enriches them with
 * author profiles fetched over HTTP from the monolith (stubbed with WireMock).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CommentControllerIntegrationTest {

  private static WireMockServer monolith;

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate rest;

  @BeforeAll
  static void startMonolith() {
    monolith = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    monolith.start();
  }

  @AfterAll
  static void stopMonolith() {
    monolith.stop();
  }

  @AfterEach
  void reset() {
    monolith.resetAll();
  }

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("monolith.service.url", () -> "http://localhost:" + monolith.port());
  }

  private String base() {
    return "http://localhost:" + port;
  }

  private void stubProfile(String userId, String username, boolean following) {
    monolith.stubFor(
        get(urlPathEqualTo("/internal/profiles"))
            .withQueryParam(
                "userId", com.github.tomakehurst.wiremock.client.WireMock.equalTo(userId))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\""
                            + userId
                            + "\",\"username\":\""
                            + username
                            + "\",\"bio\":\"bio\",\"image\":\"img\",\"following\":"
                            + following
                            + "}")));
  }

  @Test
  void should_create_comment_and_enrich_author_from_monolith() {
    stubProfile("user-1", "alice", true);

    Map<String, Object> body = new HashMap<>();
    body.put("body", "hello world");
    body.put("userId", "user-1");
    body.put("articleId", "article-xyz");

    ResponseEntity<Map> created = rest.postForEntity(base() + "/comments", body, Map.class);

    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Map<?, ?> comment = (Map<?, ?>) created.getBody().get("comment");
    assertThat(comment.get("body")).isEqualTo("hello world");
    Map<?, ?> author = (Map<?, ?>) comment.get("author");
    assertThat(author.get("username")).isEqualTo("alice");
    assertThat(author.get("following")).isEqualTo(true);
    monolith.verify(1, getRequestedFor(urlPathEqualTo("/internal/profiles")));
  }

  @Test
  void should_list_comments_by_article() {
    stubProfile("user-2", "bob", false);

    Map<String, Object> body = new HashMap<>();
    body.put("body", "first comment");
    body.put("userId", "user-2");
    body.put("articleId", "article-list");
    rest.postForEntity(base() + "/comments", body, Map.class);

    ResponseEntity<Map> list =
        rest.getForEntity(base() + "/comments?articleId=article-list", Map.class);
    assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
    java.util.List<?> comments = (java.util.List<?>) list.getBody().get("comments");
    assertThat(comments).isNotEmpty();
  }

  @Test
  void should_delete_comment() {
    stubProfile("user-3", "carol", false);

    Map<String, Object> body = new HashMap<>();
    body.put("body", "to delete");
    body.put("userId", "user-3");
    body.put("articleId", "article-del");
    ResponseEntity<Map> created = rest.postForEntity(base() + "/comments", body, Map.class);
    String id = (String) ((Map<?, ?>) created.getBody().get("comment")).get("id");

    rest.delete(base() + "/comments/" + id);

    ResponseEntity<Map> after = rest.getForEntity(base() + "/comments/" + id, Map.class);
    assertThat(after.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_degrade_gracefully_when_monolith_unavailable() {
    Map<String, Object> body = new HashMap<>();
    body.put("body", "no author service");
    body.put("userId", "user-unknown");
    body.put("articleId", "article-1");

    ResponseEntity<Map> created = rest.postForEntity(base() + "/comments", body, Map.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Map<?, ?> comment = (Map<?, ?>) created.getBody().get("comment");
    Map<?, ?> author = (Map<?, ?>) comment.get("author");
    assertThat(author.get("following")).isEqualTo(false);
  }

  private static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder getRequestedFor(
      com.github.tomakehurst.wiremock.matching.UrlPattern urlPattern) {
    return com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(urlPattern);
  }
}
