package io.spring.favorites.api;

import io.spring.favorites.application.FavoritesQueryService;
import io.spring.favorites.application.data.ArticleData;
import io.spring.favorites.core.favorite.ArticleFavorite;
import io.spring.favorites.core.favorite.ArticleFavoriteRepository;
import io.spring.favorites.core.user.CurrentUser;
import io.spring.favorites.infrastructure.monolith.MonolithClient;
import io.spring.favorites.infrastructure.monolith.dto.ArticleDto;
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
@RequestMapping(path = "articles/{slug}/favorite")
@AllArgsConstructor
public class ArticleFavoriteApi {
  private ArticleFavoriteRepository articleFavoriteRepository;
  private MonolithClient monolithClient;
  private FavoritesQueryService favoritesQueryService;

  @PostMapping
  public ResponseEntity<HashMap<String, Object>> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal CurrentUser user) {
    ArticleDto article = monolithClient.findArticleBySlug(slug, user.getId());
    articleFavoriteRepository.save(new ArticleFavorite(article.getId(), user.getId()));
    return responseArticleData(favoritesQueryService.articleData(article, user.getId()));
  }

  @DeleteMapping
  public ResponseEntity<HashMap<String, Object>> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal CurrentUser user) {
    ArticleDto article = monolithClient.findArticleBySlug(slug, user.getId());
    articleFavoriteRepository
        .find(article.getId(), user.getId())
        .ifPresent(favorite -> articleFavoriteRepository.remove(favorite));
    return responseArticleData(favoritesQueryService.articleData(article, user.getId()));
  }

  private ResponseEntity<HashMap<String, Object>> responseArticleData(
      final ArticleData articleData) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleData);
          }
        });
  }
}
