package io.spring.articleservice.controller;

import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.dto.request.UpdateArticleRequest;
import io.spring.articleservice.exception.ResourceNotFoundException;
import io.spring.articleservice.service.ArticleService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{slug}")
@RequiredArgsConstructor
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  public ResponseEntity<Map<String, ArticleDto>> getArticle(
      @PathVariable String slug, @AuthenticationPrincipal String userId) {
    ArticleDto article =
        articleService.findBySlug(slug, userId).orElseThrow(ResourceNotFoundException::new);
    return ResponseEntity.ok(Map.of("article", article));
  }

  @PutMapping
  public ResponseEntity<Map<String, ArticleDto>> updateArticle(
      @PathVariable String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody Map<String, UpdateArticleRequest> wrapper) {
    UpdateArticleRequest request = wrapper.get("article");
    if (request == null) {
      return ResponseEntity.badRequest().build();
    }
    ArticleDto article = articleService.updateArticle(slug, request, userId);
    return ResponseEntity.ok(Map.of("article", article));
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteArticle(
      @PathVariable String slug, @AuthenticationPrincipal String userId) {
    articleService.deleteArticle(slug, userId);
    return ResponseEntity.noContent().build();
  }
}
