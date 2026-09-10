package io.spring.favorites.api;

import io.spring.favorites.application.FavoritesQueryService;
import io.spring.favorites.application.data.ArticleFavoriteData;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Service-side contract consumed by the monolith's article read paths and GraphQL resolvers. */
@RestController
@RequestMapping(path = "/internal/favorites")
public class InternalFavoritesApi {
  private final FavoritesQueryService favoritesQueryService;

  public InternalFavoritesApi(FavoritesQueryService favoritesQueryService) {
    this.favoritesQueryService = favoritesQueryService;
  }

  @GetMapping
  public ResponseEntity<HashMap<String, Object>> favorites(
      @RequestParam(value = "articleIds", required = false) String articleIds,
      @RequestParam(value = "viewerId", required = false) String viewerId) {
    List<ArticleFavoriteData> favorites =
        favoritesQueryService.favoritesOf(ids(articleIds), viewerId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("favorites", favorites);
          }
        });
  }

  private List<String> ids(String articleIds) {
    if (articleIds == null || articleIds.trim().isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.stream(articleIds.split(","))
        .map(String::trim)
        .filter(id -> !id.isEmpty())
        .distinct()
        .collect(Collectors.toList());
  }
}
