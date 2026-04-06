package io.spring.article.api;

import io.spring.article.core.ArticleRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/articles")
@AllArgsConstructor
public class InternalArticleApi {
  private ArticleRepository articleRepository;

  @GetMapping("/{slug}/exists")
  public ResponseEntity<Boolean> articleExists(@PathVariable("slug") String slug) {
    return ResponseEntity.ok(articleRepository.findBySlug(slug).isPresent());
  }

  @GetMapping("/{slug}/id")
  public ResponseEntity<String> getArticleId(@PathVariable("slug") String slug) {
    return articleRepository
        .findBySlug(slug)
        .map(article -> ResponseEntity.ok(article.getId()))
        .orElse(ResponseEntity.notFound().build());
  }
}
