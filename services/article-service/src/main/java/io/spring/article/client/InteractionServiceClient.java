package io.spring.article.client;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class InteractionServiceClient {
  private final RestTemplate restTemplate;
  private final String interactionServiceUrl;

  public InteractionServiceClient(
      RestTemplate restTemplate,
      @Value("${services.interaction-service.url:http://localhost:8083}")
          String interactionServiceUrl) {
    this.restTemplate = restTemplate;
    this.interactionServiceUrl = interactionServiceUrl;
  }

  public List<String> getArticlesFavoritedByUser(String userId) {
    try {
      String[] articleIds =
          restTemplate.getForObject(
              interactionServiceUrl + "/internal/favorites/by-user/" + userId, String[].class);
      return articleIds != null ? Arrays.asList(articleIds) : Collections.emptyList();
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }
}
