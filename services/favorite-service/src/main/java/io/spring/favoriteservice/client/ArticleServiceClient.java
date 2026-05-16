package io.spring.favoriteservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.spring.favoriteservice.application.data.ArticleData;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

  public Optional<ArticleData> getArticleBySlug(String slug, String authToken) {
    try {
      HttpHeaders headers = new HttpHeaders();
      if (authToken != null) {
        headers.set("Authorization", authToken);
      }
      HttpEntity<Void> entity = new HttpEntity<>(headers);
      ResponseEntity<ArticleResponse> response =
          restTemplate.exchange(
              articleServiceUrl + "/articles/" + slug,
              HttpMethod.GET,
              entity,
              ArticleResponse.class);
      if (response.getBody() != null) {
        return Optional.ofNullable(response.getBody().getArticle());
      }
      return Optional.empty();
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public Optional<String> getArticleIdBySlug(String slug, String authToken) {
    return getArticleBySlug(slug, authToken).map(ArticleData::getId);
  }

  @Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  static class ArticleResponse {
    @JsonProperty("article")
    private ArticleData article;
  }
}
