package io.spring.article.api;

import io.spring.article.api.exception.NoAuthorizationException;
import io.spring.article.api.exception.ResourceNotFoundException;
import io.spring.article.application.ArticleQueryService;
import io.spring.article.application.article.ArticleCommandService;
import io.spring.article.application.article.UpdateArticleParam;
import io.spring.article.application.data.ArticleData;
import io.spring.article.core.Article;
import io.spring.article.core.ArticleRepository;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleQueryService
        .findBySlug(slug)
        .map(articleData -> ResponseEntity.ok(articleResponse(articleData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!userId.equals(article.getUserId())) {
                throw new NoAuthorizationException();
              }
              Article updatedArticle =
                  articleCommandService.updateArticle(article, updateArticleParam);
              return ResponseEntity.ok(
                  articleResponse(
                      articleQueryService.findBySlug(updatedArticle.getSlug()).get()));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!userId.equals(article.getUserId())) {
                throw new NoAuthorizationException();
              }
              articleRepository.remove(article);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
