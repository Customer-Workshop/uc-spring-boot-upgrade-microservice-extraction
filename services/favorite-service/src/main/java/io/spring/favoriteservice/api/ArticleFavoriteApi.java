package io.spring.favoriteservice.api;

import io.spring.common.exception.ResourceNotFoundException;
import io.spring.favoriteservice.application.data.ArticleData;
import io.spring.favoriteservice.client.ArticleServiceClient;
import io.spring.favoriteservice.core.favorite.ArticleFavorite;
import io.spring.favoriteservice.core.favorite.ArticleFavoriteRepository;
import io.spring.favoriteservice.infrastructure.mybatis.readservice.ArticleFavoritesReadService;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
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
  private ArticleServiceClient articleServiceClient;
  private ArticleFavoritesReadService articleFavoritesReadService;

  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal Object user,
      HttpServletRequest request) {
    String authToken = request.getHeader("Authorization");
    ArticleData article =
        articleServiceClient
            .getArticleBySlug(slug, authToken)
            .orElseThrow(ResourceNotFoundException::new);
    String userId = getUserId(user);
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), userId);
    articleFavoriteRepository.save(articleFavorite);

    article.setFavorited(true);
    article.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(article.getId()));
    return responseArticleData(article);
  }

  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal Object user,
      HttpServletRequest request) {
    String authToken = request.getHeader("Authorization");
    ArticleData article =
        articleServiceClient
            .getArticleBySlug(slug, authToken)
            .orElseThrow(ResourceNotFoundException::new);
    String userId = getUserId(user);
    articleFavoriteRepository
        .find(article.getId(), userId)
        .ifPresent(favorite -> articleFavoriteRepository.remove(favorite));

    article.setFavorited(false);
    article.setFavoritesCount(articleFavoritesReadService.articleFavoriteCount(article.getId()));
    return responseArticleData(article);
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

  private String getUserId(Object user) {
    if (user instanceof io.spring.favoriteservice.client.UserServiceClient.UserData) {
      return ((io.spring.favoriteservice.client.UserServiceClient.UserData) user).getId();
    }
    throw new IllegalStateException("Unknown user type");
  }
}
