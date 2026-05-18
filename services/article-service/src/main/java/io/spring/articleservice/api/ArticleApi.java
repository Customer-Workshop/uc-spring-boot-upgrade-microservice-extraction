package io.spring.articleservice.api;

import io.spring.articleservice.api.exception.NoAuthorizationException;
import io.spring.articleservice.api.exception.ResourceNotFoundException;
import io.spring.articleservice.domain.Article;
import io.spring.articleservice.domain.ArticleData;
import io.spring.articleservice.domain.User;
import io.spring.articleservice.repository.ArticleMapper;
import io.spring.articleservice.service.ArticleCommandService;
import io.spring.articleservice.service.ArticleQueryService;
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
  private ArticleMapper articleMapper;
  private ArticleCommandService articleCommandService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    return articleQueryService
        .findBySlug(slug, user)
        .map(articleData -> ResponseEntity.ok(articleResponse(articleData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @PutMapping
  public ResponseEntity<?> updateArticle(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody UpdateArticleParam updateArticleParam) {
    Article article = articleMapper.findBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    if (!user.getId().equals(article.getUserId())) {
      throw new NoAuthorizationException();
    }
    Article updatedArticle =
        articleCommandService.updateArticle(
            article,
            updateArticleParam.getTitle(),
            updateArticleParam.getDescription(),
            updateArticleParam.getBody());
    return ResponseEntity.ok(
        articleResponse(articleQueryService.findBySlug(updatedArticle.getSlug(), user).get()));
  }

  @DeleteMapping
  public ResponseEntity deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article = articleMapper.findBySlug(slug);
    if (article == null) {
      throw new ResourceNotFoundException();
    }
    if (!user.getId().equals(article.getUserId())) {
      throw new NoAuthorizationException();
    }
    articleMapper.delete(article.getId());
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
