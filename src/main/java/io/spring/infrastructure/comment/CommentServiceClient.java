package io.spring.infrastructure.comment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * HTTP client the monolith uses to talk to the extracted Comments microservice. Replaces the
 * previous direct MyBatis persistence of the Comments bounded context.
 */
@Component
public class CommentServiceClient {

  private static final Logger log = LoggerFactory.getLogger(CommentServiceClient.class);

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public CommentServiceClient(
      RestTemplate restTemplate, @Value("${comments.service.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  public CommentResponseDto create(
      String id, String articleId, String userId, String body, String viewerId) {
    Map<String, String> request = new HashMap<>();
    request.put("id", id);
    request.put("articleId", articleId);
    request.put("userId", userId);
    request.put("body", body);
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments")
            .queryParamIfPresent("viewerId", optional(viewerId))
            .toUriString();
    return restTemplate.postForObject(url, request, CommentResponseDto.class);
  }

  public Optional<CommentResponseDto> findById(String id, String viewerId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments/{id}")
            .queryParamIfPresent("viewerId", optional(viewerId))
            .buildAndExpand(id)
            .toUriString();
    try {
      return Optional.ofNullable(restTemplate.getForObject(url, CommentResponseDto.class));
    } catch (HttpClientErrorException.NotFound e) {
      return Optional.empty();
    }
  }

  public List<CommentResponseDto> findByArticleId(String articleId, String viewerId) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments")
            .queryParam("articleId", articleId)
            .queryParamIfPresent("viewerId", optional(viewerId))
            .toUriString();
    CommentResponseDto[] result = restTemplate.getForObject(url, CommentResponseDto[].class);
    return result == null ? java.util.Collections.emptyList() : Arrays.asList(result);
  }

  public List<CommentResponseDto> findByArticleIdWithCursor(
      String articleId, String viewerId, String cursor, String direction) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments/cursor")
            .queryParam("articleId", articleId)
            .queryParam("direction", direction)
            .queryParamIfPresent("viewerId", optional(viewerId))
            .queryParamIfPresent("cursor", optional(cursor))
            .toUriString();
    CommentResponseDto[] result = restTemplate.getForObject(url, CommentResponseDto[].class);
    return result == null ? java.util.Collections.emptyList() : Arrays.asList(result);
  }

  public void delete(String id) {
    String url =
        UriComponentsBuilder.fromHttpUrl(baseUrl)
            .path("/comments/{id}")
            .buildAndExpand(id)
            .toUriString();
    try {
      restTemplate.delete(url);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() != HttpStatus.NOT_FOUND) {
        throw e;
      }
    } catch (RestClientException e) {
      log.warn("Failed to delete comment {} in comments service: {}", id, e.getMessage());
      throw e;
    }
  }

  private static Optional<String> optional(String value) {
    return (value == null || value.isEmpty()) ? Optional.empty() : Optional.of(value);
  }
}
