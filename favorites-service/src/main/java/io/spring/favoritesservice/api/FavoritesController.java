package io.spring.favoritesservice.api;

import io.spring.favoritesservice.application.dto.ArticleDto;
import io.spring.favoritesservice.core.ArticleFavorite;
import io.spring.favoritesservice.core.ArticleFavoriteRepository;
import io.spring.favoritesservice.infrastructure.client.MonolithClient;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FavoritesController {
  private final ArticleFavoriteRepository articleFavoriteRepository;
  private final MonolithClient monolithClient;

  public FavoritesController(
      ArticleFavoriteRepository articleFavoriteRepository, MonolithClient monolithClient) {
    this.articleFavoriteRepository = articleFavoriteRepository;
    this.monolithClient = monolithClient;
  }

  @PostMapping("/articles/{slug}/favorite")
  public ResponseEntity<?> favoriteArticle(
      @PathVariable("slug") String slug,
      @RequestHeader(value = "X-User-Id", required = true) String userId) {
    ArticleDto article = monolithClient.getArticleBySlug(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(favorite);
    return ResponseEntity.ok(favoriteResponse(article.getId(), userId));
  }

  @DeleteMapping("/articles/{slug}/favorite")
  public ResponseEntity<?> unfavoriteArticle(
      @PathVariable("slug") String slug,
      @RequestHeader(value = "X-User-Id", required = true) String userId) {
    ArticleDto article = monolithClient.getArticleBySlug(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.ok(favoriteResponse(article.getId(), userId));
  }

  @GetMapping("/articles/{articleId}/favorites/count")
  public ResponseEntity<?> getFavoriteCount(@PathVariable("articleId") String articleId) {
    int count = articleFavoriteRepository.countByArticleId(articleId);
    Map<String, Object> response = new HashMap<>();
    response.put("count", count);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/articles/{articleId}/favorites/status")
  public ResponseEntity<?> getFavoriteStatus(
      @PathVariable("articleId") String articleId, @RequestParam("userId") String userId) {
    boolean favorited = articleFavoriteRepository.isUserFavorite(articleId, userId);
    Map<String, Object> response = new HashMap<>();
    response.put("favorited", favorited);
    return ResponseEntity.ok(response);
  }

  private Map<String, Object> favoriteResponse(String articleId, String userId) {
    boolean favorited = articleFavoriteRepository.isUserFavorite(articleId, userId);
    int count = articleFavoriteRepository.countByArticleId(articleId);
    Map<String, Object> response = new HashMap<>();
    response.put("favorited", favorited);
    response.put("favoritesCount", count);
    return response;
  }
}
