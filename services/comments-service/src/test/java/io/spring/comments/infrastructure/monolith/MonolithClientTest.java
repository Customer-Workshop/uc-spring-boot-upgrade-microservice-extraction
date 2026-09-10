package io.spring.comments.infrastructure.monolith;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.comments.api.exception.MonolithUnavailableException;
import io.spring.comments.api.exception.ResourceNotFoundException;
import io.spring.comments.infrastructure.monolith.dto.ArticleDTO;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

public class MonolithClientTest {
  private static final String BASE_URL = "http://monolith:8080";

  private MockServerRestTemplateCustomizer customizer;
  private MonolithClient monolithClient;

  @BeforeEach
  public void setUp() {
    customizer = new MockServerRestTemplateCustomizer();
    monolithClient = new MonolithClient(new RestTemplateBuilder(customizer), BASE_URL, 2000, 5000);
  }

  private MockRestServiceServer server() {
    return customizer.getServer();
  }

  @Test
  public void should_read_article_by_slug() {
    server()
        .expect(requestTo(BASE_URL + "/internal/articles/a-slug"))
        .andRespond(
            withSuccess(
                "{\"article\":{\"id\":\"article-1\",\"slug\":\"a-slug\",\"title\":\"t\","
                    + "\"createdAt\":\"2021-01-01T00:00:00.000Z\","
                    + "\"author\":{\"id\":\"user-1\",\"username\":\"john\"}}}",
                MediaType.APPLICATION_JSON));

    ArticleDTO article = monolithClient.findArticleBySlug("a-slug");
    Assertions.assertEquals("article-1", article.getId());
    Assertions.assertEquals("user-1", article.getAuthor().getId());
  }

  @Test
  public void should_read_profiles_by_ids() {
    server()
        .expect(requestTo(BASE_URL + "/internal/users?ids=user-1,user-2&viewerId=user-3"))
        .andRespond(
            withSuccess(
                "{\"profiles\":[{\"id\":\"user-1\",\"username\":\"a\",\"following\":true},"
                    + "{\"id\":\"user-2\",\"username\":\"b\",\"following\":false}]}",
                MediaType.APPLICATION_JSON));

    List<ProfileDTO> profiles =
        monolithClient.findProfilesByIds(Arrays.asList("user-1", "user-2"), "user-3");
    Assertions.assertEquals(2, profiles.size());
    Assertions.assertTrue(profiles.get(0).isFollowing());
  }

  @Test
  public void should_map_404_to_resource_not_found() {
    server()
        .expect(requestTo(BASE_URL + "/internal/articles/missing"))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> monolithClient.findArticleBySlug("missing"));
  }

  @Test
  public void should_map_server_error_to_monolith_unavailable_with_endpoint() {
    server()
        .expect(requestTo(BASE_URL + "/internal/users/user-1?viewerId=user-1"))
        .andRespond(withServerError());

    MonolithUnavailableException exception =
        Assertions.assertThrows(
            MonolithUnavailableException.class,
            () -> monolithClient.findProfileById("user-1", "user-1"));
    Assertions.assertTrue(exception.getMessage().contains("/internal/users/user-1"));
  }

  @Test
  public void should_map_garbage_body_to_monolith_unavailable() {
    server()
        .expect(requestTo(BASE_URL + "/internal/users/user-1?viewerId=user-1"))
        .andRespond(withSuccess("not json at all", MediaType.APPLICATION_JSON));

    MonolithUnavailableException exception =
        Assertions.assertThrows(
            MonolithUnavailableException.class,
            () -> monolithClient.findProfileById("user-1", "user-1"));
    Assertions.assertTrue(exception.getMessage().contains("/internal/users/user-1"));
  }

  @Test
  public void should_map_missing_payload_to_monolith_unavailable() {
    server()
        .expect(requestTo(BASE_URL + "/internal/articles/a-slug"))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    Assertions.assertThrows(
        MonolithUnavailableException.class, () -> monolithClient.findArticleBySlug("a-slug"));
  }
}
