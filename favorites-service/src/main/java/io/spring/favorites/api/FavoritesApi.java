package io.spring.favorites.api;

import io.spring.favorites.application.ArticleFavoritesQueryService;
import io.spring.favorites.application.data.ArticleFavoriteCount;
import io.spring.favorites.core.ArticleFavorite;
import io.spring.favorites.core.ArticleFavoriteRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/favorites")
public class FavoritesApi {
  private final ArticleFavoriteRepository articleFavoriteRepository;
  private final ArticleFavoritesQueryService articleFavoritesQueryService;

  @Autowired
  public FavoritesApi(
      ArticleFavoriteRepository articleFavoriteRepository,
      ArticleFavoritesQueryService articleFavoritesQueryService) {
    this.articleFavoriteRepository = articleFavoriteRepository;
    this.articleFavoritesQueryService = articleFavoritesQueryService;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> createFavorite(@RequestBody Map<String, String> body) {
    String articleId = body.get("articleId");
    String userId = body.get("userId");
    ArticleFavorite articleFavorite = new ArticleFavorite(articleId, userId);
    articleFavoriteRepository.save(articleFavorite);
    Map<String, Object> response = new HashMap<>();
    response.put("articleId", articleId);
    response.put("userId", userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> findFavorite(
      @RequestParam("articleId") String articleId, @RequestParam("userId") String userId) {
    Optional<ArticleFavorite> favorite = articleFavoriteRepository.find(articleId, userId);
    if (favorite.isPresent()) {
      Map<String, Object> response = new HashMap<>();
      response.put("articleId", favorite.get().getArticleId());
      response.put("userId", favorite.get().getUserId());
      return ResponseEntity.ok(response);
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> removeFavorite(
      @RequestParam("articleId") String articleId, @RequestParam("userId") String userId) {
    Optional<ArticleFavorite> favorite = articleFavoriteRepository.find(articleId, userId);
    favorite.ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/count")
  public ResponseEntity<?> getFavoriteCount(
      @RequestParam(value = "articleId", required = false) String articleId,
      @RequestParam(value = "articleIds", required = false) List<String> articleIds) {
    if (articleIds != null && !articleIds.isEmpty()) {
      List<ArticleFavoriteCount> counts =
          articleFavoritesQueryService.articlesFavoriteCount(articleIds);
      return ResponseEntity.ok(counts);
    } else if (articleId != null) {
      int count = articleFavoritesQueryService.articleFavoriteCount(articleId);
      Map<String, Object> response = new HashMap<>();
      response.put("id", articleId);
      response.put("count", count);
      return ResponseEntity.ok(response);
    }
    return ResponseEntity.badRequest().build();
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<Set<String>> getUserFavorites(
      @PathVariable("userId") String userId,
      @RequestParam(value = "articleIds", required = false) List<String> articleIds) {
    if (articleIds != null && !articleIds.isEmpty()) {
      Set<String> favorites = articleFavoritesQueryService.userFavorites(articleIds, userId);
      return ResponseEntity.ok(favorites);
    }
    return ResponseEntity.ok(Set.of());
  }

  @GetMapping("/user/{userId}/check")
  public ResponseEntity<Set<String>> checkUserFavorites(
      @PathVariable("userId") String userId, @RequestParam("articleIds") List<String> articleIds) {
    Set<String> favorites = articleFavoritesQueryService.userFavorites(articleIds, userId);
    return ResponseEntity.ok(favorites);
  }

  @GetMapping("/is-favorite")
  public ResponseEntity<Map<String, Boolean>> isFavorite(
      @RequestParam("userId") String userId, @RequestParam("articleId") String articleId) {
    boolean isFavorite = articleFavoritesQueryService.isUserFavorite(userId, articleId);
    Map<String, Boolean> response = new HashMap<>();
    response.put("isFavorite", isFavorite);
    return ResponseEntity.ok(response);
  }
}
