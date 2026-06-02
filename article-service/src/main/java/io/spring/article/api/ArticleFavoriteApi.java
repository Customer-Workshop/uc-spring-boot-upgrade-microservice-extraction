package io.spring.article.api;

import io.spring.article.api.exception.ResourceNotFoundException;
import io.spring.article.application.ArticleQueryService;
import io.spring.article.application.data.ArticleData;
import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.favorite.ArticleFavorite;
import io.spring.article.core.favorite.ArticleFavoriteRepository;
import io.spring.article.infrastructure.client.UserProfileCacheService;
import java.util.HashMap;
import java.util.Map;
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
  private ArticleRepository articleRepository;
  private ArticleQueryService articleQueryService;
  private UserProfileCacheService userProfileCacheService;

  @PostMapping
  public ResponseEntity<?> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    userProfileCacheService.ensureProfileCached(userId);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(articleFavorite);
    return ResponseEntity.ok(
        articleResponse(articleQueryService.findBySlug(slug, userId).get()));
  }

  @DeleteMapping
  public ResponseEntity<?> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(articleFavoriteRepository::remove);
    return ResponseEntity.ok(
        articleResponse(articleQueryService.findBySlug(slug, userId).get()));
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
