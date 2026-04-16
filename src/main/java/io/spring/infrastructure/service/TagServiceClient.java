package io.spring.infrastructure.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TagServiceClient {
  private final RestTemplate restTemplate;
  private final String tagsServiceUrl;

  public TagServiceClient(
      RestTemplate restTemplate,
      @Value("${tags.service.url:http://localhost:8081}") String tagsServiceUrl) {
    this.restTemplate = restTemplate;
    this.tagsServiceUrl = tagsServiceUrl;
  }

  /**
   * Fetches all tag names from the Tags microservice.
   *
   * @return list of tag name strings
   */
  @SuppressWarnings("unchecked")
  public List<String> getAllTags() {
    ResponseEntity<Map> response =
        restTemplate.getForEntity(tagsServiceUrl + "/tags", Map.class);
    Map<String, Object> body = response.getBody();
    if (body != null && body.containsKey("tags")) {
      return (List<String>) body.get("tags");
    }
    return Collections.emptyList();
  }

  /**
   * Creates a tag via the Tags microservice (upsert semantics). Returns the tag ID.
   *
   * @param tagName the name of the tag to create or find
   * @return the tag ID
   */
  public String findOrCreateTag(String tagName) {
    Map<String, String> request = new HashMap<>();
    request.put("name", tagName);
    ResponseEntity<Map> response =
        restTemplate.postForEntity(tagsServiceUrl + "/tags", request, Map.class);
    return (String) response.getBody().get("id");
  }

  /**
   * Creates an article-tag relationship via the Tags microservice.
   *
   * @param articleId the article ID
   * @param tagId the tag ID
   */
  public void createArticleTagRelation(String articleId, String tagId) {
    Map<String, String> request = new HashMap<>();
    request.put("articleId", articleId);
    request.put("tagId", tagId);
    restTemplate.postForEntity(tagsServiceUrl + "/tags/article-tags", request, Map.class);
  }
}
