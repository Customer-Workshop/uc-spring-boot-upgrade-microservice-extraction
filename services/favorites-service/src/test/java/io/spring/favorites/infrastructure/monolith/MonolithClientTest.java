package io.spring.favorites.infrastructure.monolith;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.favorites.api.exception.MonolithUnavailableException;
import io.spring.favorites.api.exception.ResourceNotFoundException;
import io.spring.favorites.infrastructure.monolith.dto.ArticleDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

public class MonolithClientTest {

  private static final String BASE_URL = "http://monolith:8080";

  private MockRestServiceServer server;

  private MonolithClient client(MockServerRestTemplateCustomizer customizer) {
    RestTemplateBuilder builder = new RestTemplateBuilder(customizer);
    MonolithClient monolithClient = new MonolithClient(builder, BASE_URL, 100, 100);
    this.server = customizer.getServer();
    return monolithClient;
  }

  @Test
  public void should_map_the_article_envelope() {
    MonolithClient monolithClient = client(new MockServerRestTemplateCustomizer());
    server
        .expect(requestTo(BASE_URL + "/internal/articles/a-slug?viewerId=user-1"))
        .andRespond(
            withSuccess(
                "{\"article\":{\"id\":\"article-1\",\"slug\":\"a-slug\",\"title\":\"t\","
                    + "\"description\":\"d\",\"body\":\"b\","
                    + "\"createdAt\":\"2021-09-01T12:00:00.000Z\","
                    + "\"updatedAt\":\"2021-09-02T12:00:00.000Z\",\"tagList\":[\"java\"],"
                    + "\"author\":{\"id\":\"user-2\",\"username\":\"jane\",\"bio\":\"\","
                    + "\"image\":\"img\",\"following\":true}}}",
                MediaType.APPLICATION_JSON));

    ArticleDto article = monolithClient.findArticleBySlug("a-slug", "user-1");

    Assertions.assertEquals("article-1", article.getId());
    Assertions.assertEquals("2021-09-01T12:00:00.000Z", article.getCreatedAt().toString());
    Assertions.assertEquals("jane", article.getAuthor().getUsername());
    Assertions.assertTrue(article.getAuthor().isFollowing());
  }

  @Test
  public void should_map_404_to_resource_not_found() {
    MonolithClient monolithClient = client(new MockServerRestTemplateCustomizer());
    server
        .expect(requestTo(BASE_URL + "/internal/articles/missing"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> monolithClient.findArticleBySlug("missing", null));
  }

  @Test
  public void should_map_5xx_to_monolith_unavailable() {
    MonolithClient monolithClient = client(new MockServerRestTemplateCustomizer());
    server.expect(requestTo(BASE_URL + "/internal/articles/boom")).andRespond(withServerError());

    MonolithUnavailableException e =
        Assertions.assertThrows(
            MonolithUnavailableException.class,
            () -> monolithClient.findArticleBySlug("boom", null));
    Assertions.assertTrue(e.getMessage().contains("/internal/articles/boom"));
  }

  @Test
  public void should_map_garbage_to_monolith_unavailable() {
    MonolithClient monolithClient = client(new MockServerRestTemplateCustomizer());
    server
        .expect(requestTo(BASE_URL + "/internal/articles/garbage"))
        .andRespond(withSuccess("not json at all", MediaType.APPLICATION_JSON));

    MonolithUnavailableException e =
        Assertions.assertThrows(
            MonolithUnavailableException.class,
            () -> monolithClient.findArticleBySlug("garbage", null));
    Assertions.assertTrue(e.getMessage().contains("/internal/articles/garbage"));
  }

  @Test
  public void should_reject_a_body_without_an_article() {
    MonolithClient monolithClient = client(new MockServerRestTemplateCustomizer());
    server
        .expect(requestTo(BASE_URL + "/internal/articles/empty"))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    Assertions.assertThrows(
        MonolithUnavailableException.class, () -> monolithClient.findArticleBySlug("empty", null));
  }

  @Test
  public void should_map_the_profile_envelope() {
    MonolithClient monolithClient = client(new MockServerRestTemplateCustomizer());
    server
        .expect(requestTo(BASE_URL + "/internal/users/user-2?viewerId=user-1"))
        .andRespond(
            withSuccess(
                "{\"profile\":{\"id\":\"user-2\",\"username\":\"jane\",\"bio\":\"b\","
                    + "\"image\":\"img\",\"following\":false}}",
                MediaType.APPLICATION_JSON));

    Assertions.assertEquals(
        "jane", monolithClient.findProfileById("user-2", "user-1").getUsername());
  }
}
