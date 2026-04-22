package io.spring.infrastructure.service.comments;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

  public CommentServiceResponse createComment(String body, String userId, String articleId) {
    CreateCommentPayload payload = new CreateCommentPayload(body, userId, articleId);
    ResponseEntity<CommentServiceResponse> response =
        restTemplate.postForEntity(
            commentsServiceUrl + "/api/comments", payload, CommentServiceResponse.class);
    return response.getBody();
  }

  public List<CommentServiceResponse> getCommentsByArticle(String articleId) {
    ResponseEntity<CommentServiceResponse[]> response =
        restTemplate.getForEntity(
            commentsServiceUrl + "/api/comments/article/" + articleId,
            CommentServiceResponse[].class);
    CommentServiceResponse[] body = response.getBody();
    if (body == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(body);
  }

  public CommentServiceResponse getComment(String commentId, String articleId) {
    ResponseEntity<CommentServiceResponse> response =
        restTemplate.getForEntity(
            commentsServiceUrl + "/api/comments/" + commentId + "/article/" + articleId,
            CommentServiceResponse.class);
    return response.getBody();
  }

  public void deleteComment(String commentId, String articleId) {
    restTemplate.delete(
        commentsServiceUrl + "/api/comments/" + commentId + "/article/" + articleId);
  }
}
