package io.spring.articleservice.controller;

import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.service.FavoriteService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{slug}/favorite")
@RequiredArgsConstructor
public class ArticleFavoriteController {

  private final FavoriteService favoriteService;

  @PostMapping
  public ResponseEntity<Map<String, ArticleDto>> favoriteArticle(
      @PathVariable String slug, @AuthenticationPrincipal String userId) {
    ArticleDto article = favoriteService.favoriteArticle(slug, userId);
    return ResponseEntity.ok(Map.of("article", article));
  }

  @DeleteMapping
  public ResponseEntity<Map<String, ArticleDto>> unfavoriteArticle(
      @PathVariable String slug, @AuthenticationPrincipal String userId) {
    ArticleDto article = favoriteService.unfavoriteArticle(slug, userId);
    return ResponseEntity.ok(Map.of("article", article));
  }
}
