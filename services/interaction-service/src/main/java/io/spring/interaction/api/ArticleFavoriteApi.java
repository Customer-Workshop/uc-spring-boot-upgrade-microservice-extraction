package io.spring.interaction.api;

import io.spring.interaction.api.exception.ResourceNotFoundException;
import io.spring.interaction.client.ArticleServiceClient;
import io.spring.interaction.core.ArticleFavorite;
import io.spring.interaction.core.ArticleFavoriteRepository;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleServiceClient articleServiceClient;

  @PostMapping
  public ResponseEntity favorite(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    String articleId = articleServiceClient.getArticleId(slug);
    if (articleId == null) {
      throw new ResourceNotFoundException();
    }
    ArticleFavorite articleFavorite = new ArticleFavorite(articleId, userId);
    articleFavoriteRepository.save(articleFavorite);
    int count = articleFavoriteRepository.countByArticleId(articleId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", new HashMap<String, Object>() {
              {
                put("favorited", true);
                put("favoritesCount", count);
              }
            });
          }
        });
  }

  @DeleteMapping
  public ResponseEntity unfavorite(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    String articleId = articleServiceClient.getArticleId(slug);
    if (articleId == null) {
      throw new ResourceNotFoundException();
    }
    articleFavoriteRepository
        .find(articleId, userId)
        .ifPresent(articleFavoriteRepository::remove);
    int count = articleFavoriteRepository.countByArticleId(articleId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", new HashMap<String, Object>() {
              {
                put("favorited", false);
                put("favoritesCount", count);
              }
            });
          }
        });
  }
}
