package io.spring.comments;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.spring.comments.application.data.ProfileData;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Verifies the Comments microservice serves its REST API over HTTP and, in turn, calls the monolith
 * (stubbed with WireMock) to enrich comment authors — i.e. bidirectional service communication.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommentServiceIntegrationTest {

  private static WireMockServer monolith;

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate rest;

  @BeforeAll
  static void startMonolithStub() {
    monolith = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    monolith.start();
    monolith.stubFor(
        get(urlPathEqualTo("/internal/profiles/author-1"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"author-1\",\"username\":\"jane\",\"bio\":\"bio\","
                            + "\"image\":\"img\",\"following\":true}")));
  }

  @AfterAll
  static void stopMonolithStub() {
    if (monolith != null) {
      monolith.stop();
    }
  }

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("monolith.url", () -> "http://localhost:" + monolith.port());
    registry.add("spring.datasource.url", () -> "jdbc:sqlite:build/it-comments.db");
  }

  private String baseUrl() {
    return "http://localhost:" + port + "/comments";
  }

  @Test
  void should_create_read_and_delete_comment_and_enrich_author_from_monolith() {
    String articleId = "it-article-" + System.nanoTime();

    Map<String, String> body = new HashMap<>();
    body.put("body", "a great comment");
    body.put("articleId", articleId);
    body.put("userId", "author-1");

    ResponseEntity<CommentView> created =
        rest.postForEntity(baseUrl() + "?viewerId=viewer-9", body, CommentView.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    CommentView createdComment = created.getBody();
    assertThat(createdComment).isNotNull();
    assertThat(createdComment.body).isEqualTo("a great comment");
    assertThat(createdComment.createdAt).isNotNull();
    assertThat(createdComment.author.getUsername()).isEqualTo("jane");
    assertThat(createdComment.author.isFollowing()).isTrue();

    // microservice actually called the monolith to resolve the author
    monolith.verify(getRequestedFor(urlPathEqualTo("/internal/profiles/author-1")));

    String id = createdComment.id;

    ResponseEntity<CommentView[]> listed =
        rest.getForEntity(baseUrl() + "?articleId={articleId}", CommentView[].class, articleId);
    assertThat(listed.getBody()).isNotNull();
    assertThat(listed.getBody()).extracting(c -> c.id).contains(id);

    ResponseEntity<CommentView> fetched =
        rest.getForEntity(baseUrl() + "/{id}", CommentView.class, id);
    assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(fetched.getBody().userId).isEqualTo("author-1");

    rest.delete(baseUrl() + "/{id}", id);

    ResponseEntity<String> afterDelete = rest.getForEntity(baseUrl() + "/{id}", String.class, id);
    assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  /** Client-side view mirroring how the monolith deserializes comments (ISO timestamps). */
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class CommentView {
    public String id;
    public String body;
    public String userId;
    public String articleId;
    public String createdAt;
    public String updatedAt;

    @JsonProperty("author")
    public ProfileData author;
  }
}
