package io.spring.articleservice.api;

import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.core.favorite.ArticleFavorite;
import io.spring.articleservice.core.favorite.ArticleFavoriteRepository;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
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
  private ArticleRepository articleRepository;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleReadService articleReadService;

  @PostMapping
  public ResponseEntity<?> favoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              ArticleFavorite favorite = new ArticleFavorite(article.getId(), userId);
              articleFavoriteRepository.save(favorite);
              ArticleData articleData = articleReadService.findById(article.getId());
              return ResponseEntity.ok(articleResponse(articleData));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping
  public ResponseEntity<?> unfavoriteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article ->
                articleFavoriteRepository
                    .find(article.getId(), userId)
                    .map(
                        favorite -> {
                          articleFavoriteRepository.remove(favorite);
                          ArticleData articleData = articleReadService.findById(article.getId());
                          return ResponseEntity.ok(articleResponse(articleData));
                        })
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<>() {
      {
        put("article", articleData);
      }
    };
  }
}
