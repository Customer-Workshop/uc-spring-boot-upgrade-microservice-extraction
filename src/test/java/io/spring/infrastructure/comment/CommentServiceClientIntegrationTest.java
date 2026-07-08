package io.spring.infrastructure.comment;

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
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.infrastructure.repository.RestCommentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the monolith communicates with the extracted Comments microservice over HTTP: the REST
 * client, {@link RestCommentRepository} and {@link CommentQueryService} are exercised against a
 * WireMock stand-in for the microservice.
 */
class CommentServiceClientIntegrationTest {

  private WireMockServer commentsService;
  private CommentServiceClient client;
  private RestCommentRepository repository;
  private CommentQueryService queryService;
  private User viewer;

  @BeforeEach
  void setUp() {
    commentsService = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    commentsService.start();
    String baseUrl = "http://localhost:" + commentsService.port();
    client = new CommentServiceClient(new RestTemplate(), baseUrl);
    repository = new RestCommentRepository(client);
    queryService = new CommentQueryService(client);
    viewer = new User("viewer@example.com", "viewer", "pw", "", "");
  }

  @AfterEach
  void tearDown() {
    commentsService.stop();
  }

  private String commentJson(String id, String articleId, String userId) {
    return "{\"id\":\""
        + id
        + "\",\"body\":\"nice\",\"userId\":\""
        + userId
        + "\",\"articleId\":\""
        + articleId
        + "\",\"createdAt\":\"2021-01-01T00:00:00.000Z\","
        + "\"updatedAt\":\"2021-01-01T00:00:00.000Z\","
        + "\"author\":{\"id\":\""
        + userId
        + "\",\"username\":\"jane\",\"bio\":\"b\",\"image\":\"i\",\"following\":true}}";
  }

  @Test
  void save_should_post_comment_to_microservice() {
    commentsService.stubFor(
        post(urlPathEqualTo("/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(commentJson("c-1", "a-1", "u-1"))));

    repository.save(new Comment("c-1", "nice", "u-1", "a-1", null));

    commentsService.verify(
        postRequestedFor(urlPathEqualTo("/comments"))
            .withRequestBody(matchingJsonPath("$.id", equalTo("c-1")))
            .withRequestBody(matchingJsonPath("$.articleId", equalTo("a-1")))
            .withRequestBody(matchingJsonPath("$.userId", equalTo("u-1")))
            .withRequestBody(matchingJsonPath("$.body", equalTo("nice"))));
  }

  @Test
  void findByArticleId_should_map_microservice_response() {
    commentsService.stubFor(
        get(urlPathEqualTo("/comments"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody("[" + commentJson("c-1", "a-1", "u-1") + "]")));

    List<CommentData> comments = queryService.findByArticleId("a-1", viewer);

    assertThat(comments).hasSize(1);
    CommentData data = comments.get(0);
    assertThat(data.getId()).isEqualTo("c-1");
    assertThat(data.getBody()).isEqualTo("nice");
    assertThat(data.getCreatedAt()).isNotNull();
    assertThat(data.getProfileData().getUsername()).isEqualTo("jane");
    assertThat(data.getProfileData().isFollowing()).isTrue();
    commentsService.verify(
        1,
        com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor(
            urlPathEqualTo("/comments")));
  }

  @Test
  void repository_findById_should_return_comment_only_for_matching_article() {
    commentsService.stubFor(
        get(urlPathEqualTo("/comments/c-1"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(commentJson("c-1", "a-1", "u-1"))));

    Optional<Comment> match = repository.findById("a-1", "c-1");
    assertThat(match).isPresent();
    assertThat(match.get().getUserId()).isEqualTo("u-1");

    Optional<Comment> mismatch = repository.findById("other-article", "c-1");
    assertThat(mismatch).isEmpty();
  }

  @Test
  void repository_findById_should_be_empty_on_404() {
    commentsService.stubFor(
        get(urlPathEqualTo("/comments/missing")).willReturn(aResponse().withStatus(404)));

    assertThat(repository.findById("a-1", "missing")).isEmpty();
  }

  @Test
  void remove_should_call_delete_on_microservice() {
    commentsService.stubFor(
        delete(urlEqualTo("/comments/c-1")).willReturn(aResponse().withStatus(204)));

    repository.remove(new Comment("c-1", "nice", "u-1", "a-1", null));

    commentsService.verify(deleteRequestedFor(urlEqualTo("/comments/c-1")));
  }
}
