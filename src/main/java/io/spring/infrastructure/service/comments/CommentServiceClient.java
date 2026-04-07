package io.spring.infrastructure.service.comments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class CommentServiceClient {

  private final RestTemplate restTemplate;
  private final String commentsServiceUrl;

  public CommentServiceClient(
      RestTemplate restTemplate,
      @Value("${comments.service.url:http://localhost:8081}") String commentsServiceUrl) {
    this.restTemplate = restTemplate;
    this.commentsServiceUrl = commentsServiceUrl;
  }

  public CommentServiceResponse createComment(String articleId, String body, String userId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    CreateCommentRequest request = new CreateCommentRequest(body, userId);
    ResponseEntity<CommentServiceResponse> response =
        restTemplate.postForEntity(url, request, CommentServiceResponse.class);
    return response.getBody();
  }

  public List<CommentServiceResponse> getCommentsByArticleId(String articleId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments";
    try {
      ResponseEntity<CommentServiceResponse[]> response =
          restTemplate.getForEntity(url, CommentServiceResponse[].class);
      if (response.getBody() != null) {
        return Arrays.asList(response.getBody());
      }
      return Collections.emptyList();
    } catch (HttpClientErrorException.NotFound e) {
      return Collections.emptyList();
    }
  }

  public Optional<CommentServiceResponse> getComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    try {
      ResponseEntity<CommentServiceResponse> response =
          restTemplate.getForEntity(url, CommentServiceResponse.class);
      return Optional.ofNullable(response.getBody());
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public void deleteComment(String articleId, String commentId) {
    String url = commentsServiceUrl + "/api/articles/" + articleId + "/comments/" + commentId;
    restTemplate.delete(url);
  }
}
