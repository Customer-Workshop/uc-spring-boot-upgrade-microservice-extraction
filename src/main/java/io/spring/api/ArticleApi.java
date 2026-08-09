package io.spring.api;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.UpdateArticleParam;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
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

/** Handles article operations under {@code /articles/{slug}}. */
@RestController
@RequestMapping(path = "/articles/{slug}")
@AllArgsConstructor
public class ArticleApi {
  private ArticleQueryService articleQueryService;
  private ArticleRepository articleRepository;
  private ArticleCommandService articleCommandService;

  /**
   * Handles {@code GET /articles/{slug}} and returns the article envelope.
   *
   * @param slug article slug
   * @param user authenticated principal, or {@code null} for an anonymous request
   * @return a response containing the article
   * @throws ResourceNotFoundException if no article has the supplied slug
   */
  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(articleData -> ResponseEntity.ok(articleResponse(articleData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Handles {@code PUT /articles/{slug}} and returns the updated article envelope.
   *
   * @param slug article slug
   * @param user authenticated principal used for ownership authorization
   * @param updateArticleParam validated article fields to update
   * @return a response containing the updated article
   * @throws ResourceNotFoundException if no article has the supplied slug
   * @throws NoAuthorizationException if the user does not own the article
   */
  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
                throw new NoAuthorizationException();
              }
              Article updatedArticle =
                  articleCommandService.updateArticle(article, updateArticleParam);
              return ResponseEntity.ok(
                  articleResponse(
                      articleQueryService.findBySlug(updatedArticle.getSlug(), user).get()));
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  /**
   * Handles {@code DELETE /articles/{slug}}.
   *
   * @param slug article slug
   * @param user authenticated principal used for ownership authorization
   * @return an empty no-content response
   * @throws ResourceNotFoundException if no article has the supplied slug
   * @throws NoAuthorizationException if the user does not own the article
   */
  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!AuthorizationService.canWriteArticle(user, article)) {
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
