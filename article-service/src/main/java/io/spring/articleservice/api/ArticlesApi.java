package io.spring.articleservice.api;

import io.spring.articleservice.application.ArticleQueryService;
import io.spring.articleservice.application.Page;
import io.spring.articleservice.application.article.ArticleCommandService;
import io.spring.articleservice.application.article.NewArticleParam;
import io.spring.articleservice.core.article.Article;
import java.util.HashMap;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {
  private ArticleCommandService articleCommandService;
  private ArticleQueryService articleQueryService;

  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal String userId) {
    Article article = articleCommandService.createArticle(newArticleParam, userId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put(
                "article",
                articleQueryService
                    .findById(article.getId(), userId)
                    .orElseThrow(
                        io.spring.articleservice.api.exception.ResourceNotFoundException::new));
          }
        });
  }

  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "followedUsers") java.util.List<String> followedUsers,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(
        articleQueryService.findUserFeed(followedUsers, new Page(offset, limit)));
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(tag, author, new Page(offset, limit), userId));
  }
}
