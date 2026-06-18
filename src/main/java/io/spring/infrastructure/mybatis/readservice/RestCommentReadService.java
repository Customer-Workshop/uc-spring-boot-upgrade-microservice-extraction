package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CommentReadServiceInterface;
import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("microservice")
@Primary
public class RestCommentReadService implements CommentReadServiceInterface {
  private static final Logger logger = LoggerFactory.getLogger(RestCommentReadService.class);

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public RestCommentReadService(@Value("${services.comments.url}") String commentsServiceUrl) {
    this.restTemplate = new RestTemplate();
    this.commentsServiceUrl = commentsServiceUrl;
  }

  @Override
  public CommentData findById(String id) {
    try {
      String url = commentsServiceUrl + "/api/comments/" + id + "?articleId=any";
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("comment")) {
        return mapToCommentData(response.get("comment"));
      }
      return null;
    } catch (RestClientException e) {
      logger.warn("Failed to fetch comment by id '{}': {}", id, e.getMessage());
      return null;
    }
  }

  @Override
  public List<CommentData> findByArticleId(String articleId) {
    try {
      String url = commentsServiceUrl + "/api/comments?articleId=" + articleId;
      @SuppressWarnings("unchecked")
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.containsKey("comments")) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> commentMaps =
            (List<Map<String, Object>>) response.get("comments");
        return commentMaps.stream()
            .map(this::mapToCommentData)
            .collect(java.util.stream.Collectors.toList());
      }
      return Collections.emptyList();
    } catch (RestClientException e) {
      logger.warn("Failed to fetch comments for article '{}': {}", articleId, e.getMessage());
      return Collections.emptyList();
    }
  }

  @Override
  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<DateTime> page) {
    return findByArticleId(articleId);
  }

  @SuppressWarnings("unchecked")
  private CommentData mapToCommentData(Object obj) {
    if (obj instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) obj;
      CommentData data = new CommentData();
      data.setId((String) map.get("id"));
      data.setBody((String) map.get("body"));
      data.setArticleId((String) map.get("articleId"));
      return data;
    }
    return null;
  }
}
