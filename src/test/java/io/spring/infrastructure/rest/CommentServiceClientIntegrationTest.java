package io.spring.infrastructure.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * Verifies the monolith's REST client speaks the comments microservice's HTTP contract correctly:
 * request URLs, JSON payloads and response parsing.
 */
public class CommentServiceClientIntegrationTest {

  private WireMockServer commentsService;
  private CommentServiceClient client;

  @BeforeEach
  void setUp() {
    commentsService = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    commentsService.start();
    client =
        new CommentServiceClient(
            new RestClientConfig().commentServiceRestTemplate(new RestTemplateBuilder()),
            "http://localhost:" + commentsService.port());
  }

  @AfterEach
  void tearDown() {
    commentsService.stop();
  }

  private String commentJson(String id, String articleId) {
    return "{\"id\":\""
        + id
        + "\",\"body\":\"hello\",\"userId\":\"user-1\",\"articleId\":\""
        + articleId
        + "\",\"createdAt\":\"2021-01-01T00:00:00.000Z\",\"updatedAt\":\"2021-01-01T00:00:00.000Z\","
        + "\"author\":{\"id\":\"user-1\",\"username\":\"alice\",\"bio\":\"b\",\"image\":\"i\",\"following\":true}}";
  }

  @Test
  void should_post_new_comment_to_microservice() {
    commentsService.stubFor(
        post(urlEqualTo("/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"comment\":" + commentJson("c1", "a1") + "}")));

    Comment comment = new Comment("c1", "hello", "user-1", "a1", new DateTime());
    client.create(comment);

    commentsService.verify(
        postRequestedFor(urlEqualTo("/comments"))
            .withRequestBody(matchingJsonPath("$.id", equalTo("c1")))
            .withRequestBody(matchingJsonPath("$.userId", equalTo("user-1")))
            .withRequestBody(matchingJsonPath("$.articleId", equalTo("a1"))));
  }

  @Test
  void should_fetch_raw_comment_for_authorization() {
    commentsService.stubFor(
        get(urlPathEqualTo("/comments/c1"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"comment\":" + commentJson("c1", "a1") + "}")));

    Optional<Comment> comment = client.findRawById("a1", "c1");
    assertThat(comment).isPresent();
    assertThat(comment.get().getUserId()).isEqualTo("user-1");
    assertThat(comment.get().getArticleId()).isEqualTo("a1");
  }

  @Test
  void should_parse_comment_data_with_author() {
    commentsService.stubFor(
        get(urlPathEqualTo("/comments/c1"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"comment\":" + commentJson("c1", "a1") + "}")));

    Optional<CommentData> data = client.findCommentDataById("c1", "viewer-1");
    assertThat(data).isPresent();
    assertThat(data.get().getProfileData().getUsername()).isEqualTo("alice");
    assertThat(data.get().getProfileData().isFollowing()).isTrue();
  }

  @Test
  void should_list_comments_by_article() {
    commentsService.stubFor(
        get(urlPathEqualTo("/comments"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"comments\":["
                            + commentJson("c1", "a1")
                            + ","
                            + commentJson("c2", "a1")
                            + "]}")));

    List<CommentData> comments = client.findByArticleId("a1", "viewer-1");
    assertThat(comments).hasSize(2);
  }

  @Test
  void should_delete_comment() {
    commentsService.stubFor(
        delete(urlPathEqualTo("/comments/c1")).willReturn(aResponse().withStatus(204)));

    client.delete("c1");

    commentsService.verify(deleteRequestedFor(urlPathEqualTo("/comments/c1")));
  }
}
