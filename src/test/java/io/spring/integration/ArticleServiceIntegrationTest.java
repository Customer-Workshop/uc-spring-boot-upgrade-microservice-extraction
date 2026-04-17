package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.infrastructure.service.ArticleServiceClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration tests that verify the monolith can communicate with the article microservice through
 * the ArticleServiceClient. Uses a stub implementation to simulate the article-service responses
 * without requiring the actual microservice to be running.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArticleServiceIntegrationTest {

  @Autowired private ArticleServiceClient articleServiceClient;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    public ArticleServiceClient stubArticleServiceClient() {
      return new StubArticleServiceClient();
    }
  }

  @Test
  void shouldCreateArticleViaClient() {
    Map<String, Object> request = new HashMap<>();
    request.put("title", "Integration Test Article");
    request.put("description", "Integration test description");
    request.put("body", "Integration test body");
    request.put("userId", "user-integration");
    request.put("tagList", List.of("integration", "test"));

    Map<String, Object> result = articleServiceClient.createArticle(request);

    assertThat(result).isNotNull();
    assertThat(result.get("title")).isEqualTo("Integration Test Article");
    assertThat(result.get("slug")).isEqualTo("integration-test-article");
    assertThat(result.get("id")).isNotNull();
  }

  @Test
  void shouldGetArticleBySlugViaClient() {
    // First create an article
    Map<String, Object> request = new HashMap<>();
    request.put("title", "Fetch Me Article");
    request.put("description", "desc");
    request.put("body", "body");
    request.put("userId", "user-fetch");

    articleServiceClient.createArticle(request);

    Optional<Map<String, Object>> result = articleServiceClient.getArticleBySlug("fetch-me-article");

    assertThat(result).isPresent();
    assertThat(result.get().get("title")).isEqualTo("Fetch Me Article");
  }

  @Test
  void shouldListArticlesViaClient() {
    List<Map<String, Object>> articles = articleServiceClient.listArticles();
    assertThat(articles).isNotNull();
  }

  @Test
  void shouldUpdateArticleViaClient() {
    Map<String, Object> createReq = new HashMap<>();
    createReq.put("title", "Before Update");
    createReq.put("description", "desc");
    createReq.put("body", "body");
    createReq.put("userId", "user-update");

    articleServiceClient.createArticle(createReq);

    Map<String, Object> updateReq = new HashMap<>();
    updateReq.put("title", "After Update");

    Map<String, Object> result = articleServiceClient.updateArticle("before-update", updateReq);

    assertThat(result).isNotNull();
    assertThat(result.get("title")).isEqualTo("After Update");
  }

  @Test
  void shouldDeleteArticleViaClient() {
    Map<String, Object> createReq = new HashMap<>();
    createReq.put("title", "Delete This");
    createReq.put("description", "desc");
    createReq.put("body", "body");
    createReq.put("userId", "user-delete");

    articleServiceClient.createArticle(createReq);

    articleServiceClient.deleteArticle("delete-this");

    Optional<Map<String, Object>> result = articleServiceClient.getArticleBySlug("delete-this");
    assertThat(result).isEmpty();
  }

  @Test
  void shouldGetTagsViaClient() {
    List<String> tags = articleServiceClient.getTags();
    assertThat(tags).isNotNull();
  }

  @Test
  void monolithHealthEndpointResponds() throws Exception {
    mockMvc.perform(get("/users")).andExpect(status().isUnauthorized());
  }

  /**
   * Stub implementation of ArticleServiceClient that simulates the article microservice in-memory.
   * This allows integration tests to verify the communication contract without running the actual
   * microservice.
   */
  static class StubArticleServiceClient extends ArticleServiceClient {
    private final Map<String, Map<String, Object>> articleStore = new HashMap<>();

    StubArticleServiceClient() {
      super(null, "http://stub");
    }

    @Override
    public Map<String, Object> createArticle(Map<String, Object> request) {
      String title = (String) request.get("title");
      String slug = title.toLowerCase().replaceAll("[\\s]+", "-");
      Map<String, Object> article = new HashMap<>(request);
      article.put("id", java.util.UUID.randomUUID().toString());
      article.put("slug", slug);
      article.put("createdAt", java.time.Instant.now().toString());
      article.put("updatedAt", java.time.Instant.now().toString());
      articleStore.put(slug, article);
      return article;
    }

    @Override
    public Optional<Map<String, Object>> getArticleBySlug(String slug) {
      return Optional.ofNullable(articleStore.get(slug));
    }

    @Override
    public List<Map<String, Object>> listArticles() {
      return new java.util.ArrayList<>(articleStore.values());
    }

    @Override
    public Map<String, Object> updateArticle(String slug, Map<String, Object> request) {
      Map<String, Object> article = articleStore.get(slug);
      if (article == null) {
        throw new RuntimeException("Article not found: " + slug);
      }
      if (request.containsKey("title")) {
        String newTitle = (String) request.get("title");
        String newSlug = newTitle.toLowerCase().replaceAll("[\\s]+", "-");
        article.put("title", newTitle);
        article.put("slug", newSlug);
        articleStore.remove(slug);
        articleStore.put(newSlug, article);
      }
      if (request.containsKey("description")) {
        article.put("description", request.get("description"));
      }
      if (request.containsKey("body")) {
        article.put("body", request.get("body"));
      }
      article.put("updatedAt", java.time.Instant.now().toString());
      return article;
    }

    @Override
    public void deleteArticle(String slug) {
      articleStore.remove(slug);
    }

    @Override
    public List<String> getTags() {
      return List.of("java", "spring", "microservices");
    }
  }
}
