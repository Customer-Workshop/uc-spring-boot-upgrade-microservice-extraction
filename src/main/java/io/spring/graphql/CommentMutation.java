package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@DgsComponent
public class CommentMutation {

  private final RestTemplate restTemplate;
  private final String commentServiceBaseUrl;

  public CommentMutation(
      RestTemplate restTemplate,
      @Value("${comment-service.base-url:http://localhost:8081}") String commentServiceBaseUrl) {
    this.restTemplate = restTemplate;
    this.commentServiceBaseUrl = commentServiceBaseUrl;
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  @SuppressWarnings("unchecked")
  public DataFetcherResult<CommentPayload> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    try {
      Map<String, Object> requestBody = Map.of("comment", Map.of("body", body));
      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", "application/json");
      HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
      restTemplate.postForEntity(
          commentServiceBaseUrl + "/articles/{slug}/comments", entity, Map.class, slug);
    } catch (HttpClientErrorException e) {
      throw new ResourceNotFoundException();
    }
    return DataFetcherResult.<CommentPayload>newResult()
        .data(CommentPayload.newBuilder().build())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteComment)
  public DeletionStatus removeComment(
      @InputArgument("slug") String slug, @InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    try {
      HttpHeaders headers = new HttpHeaders();
      HttpEntity<Void> entity = new HttpEntity<>(headers);
      restTemplate.exchange(
          commentServiceBaseUrl + "/articles/{slug}/comments/{id}",
          HttpMethod.DELETE,
          entity,
          Void.class,
          slug,
          commentId);
      return DeletionStatus.newBuilder().success(true).build();
    } catch (HttpClientErrorException e) {
      throw new ResourceNotFoundException();
    }
  }
}
