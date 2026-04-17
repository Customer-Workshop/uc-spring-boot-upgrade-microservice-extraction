package io.spring.article.api;

import io.spring.article.api.dto.CreateArticleRequest;
import io.spring.article.api.dto.UpdateArticleRequest;
import io.spring.article.api.dto.ArticleResponse;
import io.spring.article.domain.Article;
import io.spring.article.service.ArticleService;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@AllArgsConstructor
public class ArticleController {
  private ArticleService articleService;

  @PostMapping
  public ResponseEntity<ArticleResponse> createArticle(
      @Valid @RequestBody CreateArticleRequest request) {
    Article article =
        articleService.createArticle(
            request.getTitle(),
            request.getDescription(),
            request.getBody(),
            request.getTagList(),
            request.getUserId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ArticleResponse.from(article));
  }

  @GetMapping("/{slug}")
  public ResponseEntity<ArticleResponse> getArticle(@PathVariable("slug") String slug) {
    Optional<Article> article = articleService.findBySlug(slug);
    return article
        .map(a -> ResponseEntity.ok(ArticleResponse.from(a)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{slug}")
  public ResponseEntity<ArticleResponse> updateArticle(
      @PathVariable("slug") String slug, @Valid @RequestBody UpdateArticleRequest request) {
    Article article =
        articleService.updateArticle(
            slug, request.getTitle(), request.getDescription(), request.getBody());
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(ArticleResponse.from(article));
  }

  @DeleteMapping("/{slug}")
  public ResponseEntity<Void> deleteArticle(@PathVariable("slug") String slug) {
    boolean deleted = articleService.deleteArticle(slug);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
