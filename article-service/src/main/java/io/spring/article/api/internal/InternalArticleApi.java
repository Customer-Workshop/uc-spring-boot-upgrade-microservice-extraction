package io.spring.article.api.internal;

import io.spring.article.core.article.ArticleRepository;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API for other microservices to verify article existence and fetch article data. These
 * endpoints are not meant for external clients.
 */
@RestController
@RequestMapping(path = "/api/internal/articles")
@AllArgsConstructor
public class InternalArticleApi {
  private ArticleRepository articleRepository;

  @GetMapping("/{id}")
  public ResponseEntity<?> getArticleById(@PathVariable("id") String id) {
    return articleRepository
        .findById(id)
        .map(
            article -> {
              Map<String, Object> response = new HashMap<>();
              response.put("id", article.getId());
              response.put("slug", article.getSlug());
              response.put("title", article.getTitle());
              response.put("userId", article.getUserId());
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/slug/{slug}")
  public ResponseEntity<?> getArticleBySlug(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              Map<String, Object> response = new HashMap<>();
              response.put("id", article.getId());
              response.put("slug", article.getSlug());
              response.put("title", article.getTitle());
              response.put("userId", article.getUserId());
              return ResponseEntity.ok(response);
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}/exists")
  public ResponseEntity<Boolean> articleExists(@PathVariable("id") String id) {
    return ResponseEntity.ok(articleRepository.findById(id).isPresent());
  }
}
