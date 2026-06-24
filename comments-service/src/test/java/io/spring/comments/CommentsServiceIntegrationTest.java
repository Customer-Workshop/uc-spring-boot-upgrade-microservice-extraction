package io.spring.comments;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Boots the comments microservice and verifies (1) its REST API and (2) that it calls back into the
 * monolith over HTTP to hydrate author profiles. The monolith is replaced by a WireMock stub.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsServiceIntegrationTest {

  private static WireMockServer monolith;

  @Autowired private TestRestTemplate rest;

  @BeforeAll
  static void startMonolithStub() {
    monolith = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    monolith.start();
    monolith.stubFor(
        get(urlPathMatching("/internal/profiles/.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"user-2\",\"username\":\"jane\",\"bio\":\"bio\","
                            + "\"image\":\"img\",\"following\":false}")));
  }

  @AfterAll
  static void stopMonolithStub() {
    monolith.stop();
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("monolith.url", () -> monolith.baseUrl());
    Path db =
        Path.of(System.getProperty("java.io.tmpdir"), "comments-it-" + System.nanoTime() + ".db");
    registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + db.toAbsolutePath());
  }

  @Test
  public void should_create_then_read_enriched_comment() {
    Map<String, Object> body = new HashMap<>();
    body.put("body", "nice post");
    body.put("articleId", "article-99");
    body.put("userId", "user-2");

    ResponseEntity<JsonNode> created = rest.postForEntity("/comments", body, JsonNode.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String id = created.getBody().get("id").asText();
    assertThat(created.getBody().get("userId").asText()).isEqualTo("user-2");

    // Enriched read should call back into the (stubbed) monolith for the author profile.
    ResponseEntity<JsonNode> data =
        rest.getForEntity("/comments/" + id + "/data?viewerId=user-1", JsonNode.class);
    assertThat(data.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(data.getBody().get("author").get("username").asText()).isEqualTo("jane");
    assertThat(data.getBody().get("articleId").asText()).isEqualTo("article-99");

    monolith.verify(
        com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
            urlPathMatching("/internal/profiles/user-2")));
  }

  @Test
  public void should_list_comments_for_article() {
    Map<String, Object> body = new HashMap<>();
    body.put("body", "listed comment");
    body.put("articleId", "article-list");
    body.put("userId", "user-2");
    rest.postForEntity("/comments", body, JsonNode.class);

    ResponseEntity<JsonNode> list =
        rest.getForEntity("/comments?articleId=article-list", JsonNode.class);
    assertThat(list.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(list.getBody().isArray()).isTrue();
    assertThat(list.getBody().size()).isGreaterThanOrEqualTo(1);
    assertThat(list.getBody().get(0).get("author").get("username").asText()).isEqualTo("jane");
  }

  @Test
  public void should_delete_comment() {
    Map<String, Object> body = new HashMap<>();
    body.put("body", "to delete");
    body.put("articleId", "article-del");
    body.put("userId", "user-2");
    ResponseEntity<JsonNode> created = rest.postForEntity("/comments", body, JsonNode.class);
    String id = created.getBody().get("id").asText();

    rest.delete("/comments/" + id);

    ResponseEntity<JsonNode> raw = rest.getForEntity("/comments/" + id, JsonNode.class);
    assertThat(raw.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
