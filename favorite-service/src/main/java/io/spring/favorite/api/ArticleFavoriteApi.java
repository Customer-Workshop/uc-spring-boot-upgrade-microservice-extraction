package io.spring.favorite.api;

import io.spring.favorite.client.ArticleData;
import io.spring.favorite.client.ArticleServiceClient;
import io.spring.favorite.core.ArticleFavorite;
import io.spring.favorite.core.ArticleFavoriteRepository;
import io.spring.favorite.exception.ResourceNotFoundException;
import io.spring.favorite.readservice.ArticleFavoritesReadService;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleServiceClient articleServiceClient;
  private ArticleFavoritesReadService articleFavoritesReadService;

  @PostMapping
  public ResponseEntity<Map<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug, @RequestHeader("X-User-Id") String userId) {
    ArticleData article =
        articleServiceClient.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(articleFavorite);
    return responseFavoriteData(article, userId);
  }

  @DeleteMapping
  public ResponseEntity<Map<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug, @RequestHeader("X-User-Id") String userId) {
    ArticleData article =
        articleServiceClient.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(favorite -> articleFavoriteRepository.remove(favorite));
    return responseFavoriteData(article, userId);
  }

  private ResponseEntity<Map<String, Object>> responseFavoriteData(
      ArticleData article, String userId) {
    boolean favorited = articleFavoritesReadService.isUserFavorite(userId, article.getId());
    int favoritesCount = articleFavoritesReadService.articleFavoriteCount(article.getId());
    Map<String, Object> body = new HashMap<>();
    body.put("articleId", article.getId());
    body.put("slug", article.getSlug());
    body.put("favorited", favorited);
    body.put("favoritesCount", favoritesCount);
    return ResponseEntity.ok(body);
  }
}
