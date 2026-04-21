package io.spring.api;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
public class CommentProxyApi {

  private final RestTemplate restTemplate;
  private final String commentServiceBaseUrl;

  public CommentProxyApi(
      RestTemplate restTemplate,
      @Value("${comment-service.base-url:http://localhost:8081}") String commentServiceBaseUrl) {
    this.restTemplate = restTemplate;
    this.commentServiceBaseUrl = commentServiceBaseUrl;
  }

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @RequestBody String body,
      HttpServletRequest request) {
    HttpHeaders headers = buildHeaders(request);
    headers.set("Content-Type", "application/json");
    HttpEntity<String> entity = new HttpEntity<>(body, headers);
    try {
      ResponseEntity<Object> response =
          restTemplate.postForEntity(
              commentServiceBaseUrl + "/articles/{slug}/comments", entity, Object.class, slug);
      return ResponseEntity.status(response.getStatusCodeValue()).body(response.getBody());
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
  }

  @GetMapping
  public ResponseEntity<?> getComments(@PathVariable("slug") String slug) {
    try {
      ResponseEntity<Object> response =
          restTemplate.exchange(
              commentServiceBaseUrl + "/articles/{slug}/comments",
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<>() {},
              slug);
      return ResponseEntity.ok(response.getBody());
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      HttpServletRequest request) {
    HttpHeaders headers = buildHeaders(request);
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    try {
      restTemplate.exchange(
          commentServiceBaseUrl + "/articles/{slug}/comments/{id}",
          HttpMethod.DELETE,
          entity,
          Void.class,
          slug,
          commentId);
      return ResponseEntity.noContent().build();
    } catch (HttpClientErrorException e) {
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
  }

  private HttpHeaders buildHeaders(HttpServletRequest request) {
    HttpHeaders headers = new HttpHeaders();
    String authorization = request.getHeader("Authorization");
    if (authorization != null) {
      headers.set("Authorization", authorization);
    }
    return headers;
  }
}
