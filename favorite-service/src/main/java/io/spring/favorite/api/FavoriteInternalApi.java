package io.spring.favorite.api;

import io.spring.favorite.core.ArticleFavoriteCount;
import io.spring.favorite.readservice.ArticleFavoritesReadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/internal/favorites")
@AllArgsConstructor
public class FavoriteInternalApi {
  private ArticleFavoritesReadService articleFavoritesReadService;

  @GetMapping("/count")
  public ResponseEntity<Map<String, Integer>> getFavoriteCounts(
      @RequestParam("articleIds") List<String> articleIds) {
    List<ArticleFavoriteCount> counts =
        articleFavoritesReadService.articlesFavoriteCount(articleIds);
    Map<String, Integer> result = new HashMap<>();
    counts.forEach(c -> result.put(c.getId(), c.getCount()));
    return ResponseEntity.ok(result);
  }

  @GetMapping("/check")
  public ResponseEntity<Set<String>> checkUserFavorites(
      @RequestParam("articleIds") List<String> articleIds,
      @RequestParam("userId") String userId) {
    Set<String> favoritedIds = articleFavoritesReadService.userFavorites(articleIds, userId);
    return ResponseEntity.ok(favoritedIds);
  }

  @GetMapping("/count/{articleId}")
  public ResponseEntity<Map<String, Integer>> getSingleFavoriteCount(
      @PathVariable("articleId") String articleId) {
    int count = articleFavoritesReadService.articleFavoriteCount(articleId);
    Map<String, Integer> result = new HashMap<>();
    result.put("count", count);
    return ResponseEntity.ok(result);
  }

  @GetMapping("/is-favorited")
  public ResponseEntity<Map<String, Boolean>> isUserFavorited(
      @RequestParam("articleId") String articleId, @RequestParam("userId") String userId) {
    boolean favorited = articleFavoritesReadService.isUserFavorite(userId, articleId);
    Map<String, Boolean> result = new HashMap<>();
    result.put("favorited", favorited);
    return ResponseEntity.ok(result);
  }
}
