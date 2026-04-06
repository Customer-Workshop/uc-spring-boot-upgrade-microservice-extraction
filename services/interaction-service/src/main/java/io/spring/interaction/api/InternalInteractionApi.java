package io.spring.interaction.api;

import io.spring.interaction.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import io.spring.shared.dto.ArticleFavoriteCount;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/internal/favorites")
@AllArgsConstructor
public class InternalInteractionApi {
  private ArticleFavoritesReadService articleFavoritesReadService;

  @GetMapping("/count/{articleId}")
  public ResponseEntity<Integer> getFavoriteCount(@PathVariable("articleId") String articleId) {
    return ResponseEntity.ok(articleFavoritesReadService.articleFavoriteCount(articleId));
  }

  @PostMapping("/count/batch")
  public ResponseEntity<List<ArticleFavoriteCount>> batchGetFavoriteCounts(
      @RequestBody List<String> articleIds) {
    return ResponseEntity.ok(articleFavoritesReadService.articlesFavoriteCount(articleIds));
  }

  @GetMapping("/check/{articleId}")
  public ResponseEntity<Boolean> isUserFavorite(
      @PathVariable("articleId") String articleId, @RequestParam("userId") String userId) {
    return ResponseEntity.ok(articleFavoritesReadService.isUserFavorite(userId, articleId));
  }

  @PostMapping("/user-favorites")
  public ResponseEntity<Set<String>> getUserFavorites(
      @RequestBody List<String> articleIds, @RequestParam("userId") String userId) {
    return ResponseEntity.ok(articleFavoritesReadService.userFavorites(articleIds, userId));
  }
}
