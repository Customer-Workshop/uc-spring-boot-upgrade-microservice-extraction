package io.spring.commentservice.client;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ArticleServiceClient {

  private final RestTemplate restTemplate;
  private final String articleServiceUrl;

  public ArticleServiceClient(
      RestTemplate restTemplate,
      @Value("${article-service.url}") String articleServiceUrl) {
    this.restTemplate = restTemplate;
    this.articleServiceUrl = articleServiceUrl;
  }

  @SuppressWarnings("unchecked")
  public Optional<ArticleInfo> getArticleBySlug(String slug) {
    try {
      Map<String, Object> response =
          restTemplate.getForObject(
              articleServiceUrl + "/articles/{slug}", Map.class, slug);
      if (response != null && response.containsKey("article")) {
        Map<String, Object> article = (Map<String, Object>) response.get("article");
        String id = (String) article.get("id");
        String userId = (String) article.get("userId");
        return Optional.of(new ArticleInfo(id, userId));
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public static class ArticleInfo {
    private final String id;
    private final String userId;

    public ArticleInfo(String id, String userId) {
      this.id = id;
      this.userId = userId;
    }

    public String getId() {
      return id;
    }

    public String getUserId() {
      return userId;
    }
  }
}
