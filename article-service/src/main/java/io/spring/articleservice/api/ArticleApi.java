package io.spring.articleservice.api;

import io.spring.articleservice.application.article.UpdateArticleParam;
import io.spring.articleservice.application.data.ArticleData;
import io.spring.articleservice.core.article.ArticleRepository;
import io.spring.articleservice.infrastructure.mybatis.readservice.ArticleReadService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
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
  private ArticleRepository articleRepository;
  private ArticleReadService articleReadService;

  @GetMapping
  public ResponseEntity<?> article(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    ArticleData articleData = articleReadService.findBySlug(slug);
    if (articleData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(articleResponse(articleData));
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
              if (!article.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build();
              }
              article.update(
                  updateArticleParam.getTitle(),
                  updateArticleParam.getDescription(),
                  updateArticleParam.getBody());
              articleRepository.save(article);
              ArticleData articleData = articleReadService.findBySlug(article.getSlug());
              return ResponseEntity.ok(articleResponse(articleData));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping
  public ResponseEntity<?> deleteArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    return articleRepository
        .findBySlug(slug)
        .map(
            article -> {
              if (!article.getUserId().equals(userId)) {
                return ResponseEntity.status(403).build();
              }
              articleRepository.remove(article);
              return ResponseEntity.noContent().build();
            })
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
