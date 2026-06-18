package io.spring.infrastructure.mybatis.readservice;

import io.spring.application.CursorPageParameter;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Profile("microservice")
@Primary
public class RemoteCommentReadService implements CommentReadService {
  private final RestTemplate restTemplate;
  private final String commentServiceUrl;

  public RemoteCommentReadService(
      RestTemplate restTemplate, @Value("${comment-service.url}") String commentServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentServiceUrl = commentServiceUrl;
  }

  @Override
  @SuppressWarnings("unchecked")
  public CommentData findById(String id) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentServiceUrl + "/internal/comments/{id}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              id);
      Map<String, Object> body = response.getBody();
      if (body == null || !body.containsKey("comment")) {
        return null;
      }
      return mapToCommentData((Map<String, Object>) body.get("comment"));
    } catch (RestClientException e) {
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<CommentData> findByArticleId(String articleId) {
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              commentServiceUrl + "/internal/comments?articleId={articleId}",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              articleId);
      Map<String, Object> body = response.getBody();
      if (body == null || !body.containsKey("comments")) {
        return Collections.emptyList();
      }
      List<Map<String, Object>> comments = (List<Map<String, Object>>) body.get("comments");
      List<CommentData> result = new ArrayList<>();
      for (Map<String, Object> comment : comments) {
        result.add(mapToCommentData(comment));
      }
      return result;
    } catch (RestClientException e) {
      return Collections.emptyList();
    }
  }

  @Override
  public List<CommentData> findByArticleIdWithCursor(
      String articleId, CursorPageParameter<DateTime> page) {
    return findByArticleId(articleId);
  }

  @SuppressWarnings("unchecked")
  private CommentData mapToCommentData(Map<String, Object> map) {
    CommentData data = new CommentData();
    data.setId((String) map.get("id"));
    data.setBody((String) map.get("body"));
    data.setArticleId((String) map.get("articleId"));
    if (map.get("createdAt") != null) {
      data.setCreatedAt(DateTime.parse((String) map.get("createdAt")));
    }
    if (map.get("updatedAt") != null) {
      data.setUpdatedAt(DateTime.parse((String) map.get("updatedAt")));
    }
    Map<String, Object> author = (Map<String, Object>) map.get("author");
    if (author != null) {
      ProfileData profile =
          new ProfileData(
              (String) author.get("id"),
              (String) author.get("username"),
              (String) author.get("bio"),
              (String) author.get("image"),
              Boolean.TRUE.equals(author.get("following")));
      data.setProfileData(profile);
    }
    return data;
  }
}
