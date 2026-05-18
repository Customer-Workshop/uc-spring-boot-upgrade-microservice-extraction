package io.spring.articleservice.api;

import io.spring.articleservice.api.exception.ResourceNotFoundException;
import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.ArticleData;
import io.spring.articleservice.domain.ArticleFavorite;
import io.spring.articleservice.domain.User;
import io.spring.articleservice.repository.ArticleFavoriteMapper;
import io.spring.articleservice.repository.ArticleMapper;
import io.spring.articleservice.service.ArticleQueryService;
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
  private ArticleFavoriteMapper articleFavoriteMapper;
  private ArticleMapper articleMapper;
  private ArticleQueryService articleQueryService;

  @PostMapping
  public ResponseEntity favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article = articleMapper.findBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    articleFavoriteMapper.insert(articleFavorite);
    return responseArticleData(articleQueryService.findBySlug(slug, user).get());
  }

  @DeleteMapping
  public ResponseEntity unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article = articleMapper.findBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    ArticleFavorite favorite = articleFavoriteMapper.find(article.getId(), user.getId());
    if (favorite != null) {
      articleFavoriteMapper.delete(favorite);
    }
    return responseArticleData(articleQueryService.findBySlug(slug, user).get());
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
