package io.spring.infrastructure.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the monolith -> comments microservice HTTP contract using a stub HTTP server in place of
 * the real microservice.
 */
public class CommentServiceClientIntegrationTest {

  private WireMockServer wireMock;
  private CommentServiceClient client;

  @BeforeEach
  public void setUp() {
    wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMock.start();
    client = new CommentServiceClient(new RestTemplate(), wireMock.baseUrl());
  }

  @AfterEach
  public void tearDown() {
    wireMock.stop();
  }

  @Test
  public void should_create_comment_via_http() {
    wireMock.stubFor(
        post(urlPathEqualTo("/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"c1\",\"body\":\"hi\",\"articleId\":\"a1\",\"userId\":\"u1\","
                            + "\"createdAt\":\"2021-01-01T00:00:00.000Z\"}")));

    Comment comment = new Comment("c1", "hi", "u1", "a1", new DateTime());
    Comment created = client.create(comment);

    assertEquals("c1", created.getId());
    assertEquals("u1", created.getUserId());
    assertEquals("a1", created.getArticleId());
  }

  @Test
  public void should_find_raw_comment_for_authorization() {
    wireMock.stubFor(
        get(urlPathEqualTo("/comments/c1"))
            .withQueryParam("articleId", equalTo("a1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"c1\",\"body\":\"hi\",\"articleId\":\"a1\",\"userId\":\"u1\","
                            + "\"createdAt\":\"2021-01-01T00:00:00.000Z\"}")));

    Optional<Comment> comment = client.findRawById("a1", "c1");

    assertTrue(comment.isPresent());
    assertEquals("u1", comment.get().getUserId());
  }

  @Test
  public void should_return_empty_when_raw_comment_missing() {
    wireMock.stubFor(
        get(urlPathEqualTo("/comments/missing")).willReturn(aResponse().withStatus(404)));

    assertFalse(client.findRawById("a1", "missing").isPresent());
  }

  @Test
  public void should_fetch_enriched_comment_data() {
    wireMock.stubFor(
        get(urlPathEqualTo("/comments/c1/data"))
            .withQueryParam("viewerId", equalTo("viewer"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"id\":\"c1\",\"body\":\"hi\",\"articleId\":\"a1\","
                            + "\"createdAt\":\"2021-01-01T00:00:00.000Z\","
                            + "\"author\":{\"username\":\"jane\",\"bio\":\"b\",\"image\":\"i\","
                            + "\"following\":true}}")));

    Optional<CommentData> data = client.findCommentData("c1", "viewer");

    assertTrue(data.isPresent());
    assertEquals("a1", data.get().getArticleId());
    assertEquals("jane", data.get().getProfileData().getUsername());
    assertTrue(data.get().getProfileData().isFollowing());
  }

  @Test
  public void should_list_comments_by_article() {
    wireMock.stubFor(
        get(urlPathEqualTo("/comments"))
            .withQueryParam("articleId", equalTo("a1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "[{\"id\":\"c1\",\"body\":\"hi\",\"articleId\":\"a1\","
                            + "\"createdAt\":\"2021-01-01T00:00:00.000Z\","
                            + "\"author\":{\"username\":\"jane\"}}]")));

    List<CommentData> comments = client.findByArticleId("a1", null);

    assertEquals(1, comments.size());
    assertEquals("jane", comments.get(0).getProfileData().getUsername());
  }

  @Test
  public void should_page_comments_with_cursor() {
    wireMock.stubFor(
        get(urlPathEqualTo("/comments/cursor"))
            .withQueryParam("articleId", equalTo("a1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"comments\":[{\"id\":\"c1\",\"body\":\"hi\",\"articleId\":\"a1\","
                            + "\"createdAt\":\"2021-01-01T00:00:00.000Z\","
                            + "\"author\":{\"username\":\"jane\"}}],"
                            + "\"hasNext\":true,\"hasPrevious\":false}")));

    CursorPager<CommentData> pager =
        client.findByArticleIdWithCursor(
            "a1", null, new CursorPageParameter<>(null, 20, Direction.NEXT));

    assertEquals(1, pager.getData().size());
    assertTrue(pager.hasNext());
  }

  @Test
  public void should_delete_comment_via_http() {
    wireMock.stubFor(
        delete(urlPathEqualTo("/comments/c1")).willReturn(aResponse().withStatus(204)));

    client.delete("c1");

    wireMock.verify(
        com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor(
            urlPathEqualTo("/comments/c1")));
  }
}
