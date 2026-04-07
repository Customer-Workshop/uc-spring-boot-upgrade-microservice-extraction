package io.spring.infrastructure.service;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentsServiceClient implements CommentRepository {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;
  private final UserReadService userReadService;

  public CommentsServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl,
      UserReadService userReadService) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
    this.userReadService = userReadService;
  }

  @Override
  public void save(Comment comment) {
    String url = commentsServiceUrl + "/api/comments";
    Map<String, String> request = new HashMap<>();
    request.put("id", comment.getId());
    request.put("body", comment.getBody());
    request.put("userId", comment.getUserId());
    request.put("articleId", comment.getArticleId());
    restTemplate.postForEntity(url, request, Map.class);
  }

  @Override
  public Optional<Comment> findById(String articleId, String id) {
    String url = commentsServiceUrl + "/api/comments/{commentId}/article/{articleId}";
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              id,
              articleId);
      Map<String, Object> body = response.getBody();
      if (body == null) {
        return Optional.empty();
      }
      return Optional.of(mapToComment(body));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  @Override
  public void remove(Comment comment) {
    String url = commentsServiceUrl + "/api/comments/{commentId}";
    restTemplate.delete(url, comment.getId());
  }

  public Optional<CommentData> findCommentDataById(String id) {
    String url = commentsServiceUrl + "/api/comments/by-id/{commentId}";
    try {
      ResponseEntity<Map<String, Object>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<Map<String, Object>>() {},
              id);
      Map<String, Object> body = response.getBody();
      if (body == null) {
        return Optional.empty();
      }
      return Optional.of(mapToCommentData(body));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public List<CommentData> findCommentDataByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/comments/article/{articleId}";
    try {
      ResponseEntity<List<Map<String, Object>>> response =
          restTemplate.exchange(
              url,
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<Map<String, Object>>>() {},
              articleId);
      List<Map<String, Object>> items =
          response.getBody() != null ? response.getBody() : new ArrayList<>();
      List<CommentData> result = new ArrayList<>();
      for (Map<String, Object> item : items) {
        result.add(mapToCommentData(item));
      }
      return result;
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  private Comment mapToComment(Map<String, Object> map) {
    return new Comment(
        (String) map.get("body"), (String) map.get("userId"), (String) map.get("articleId"));
  }

  private CommentData mapToCommentData(Map<String, Object> map) {
    String id = (String) map.get("id");
    String body = (String) map.get("body");
    String articleId = (String) map.get("articleId");
    String userId = (String) map.get("userId");
    String createdAtStr = (String) map.get("createdAt");
    DateTime createdAt =
        createdAtStr != null
            ? ISODateTimeFormat.dateTime().parseDateTime(createdAtStr)
            : new DateTime();

    ProfileData profileData = null;
    if (userId != null) {
      var userData = userReadService.findById(userId);
      if (userData != null) {
        profileData =
            new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                false);
      }
    }
    if (profileData == null) {
      profileData = new ProfileData(userId, "", "", "", false);
    }

    return new CommentData(id, body, articleId, createdAt, createdAt, profileData);
  }
}
