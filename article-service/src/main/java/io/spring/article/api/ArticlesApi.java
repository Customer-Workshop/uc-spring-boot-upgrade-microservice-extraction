package io.spring.article.api;

import io.spring.article.api.exception.InvalidRequestException;
import io.spring.article.application.ArticleQueryService;
import io.spring.article.application.Page;
import io.spring.article.application.article.ArticleCommandService;
import io.spring.article.application.article.NewArticleParam;
import io.spring.article.application.data.ArticleData;
import io.spring.article.application.data.ArticleDataList;
import io.spring.article.core.article.Article;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
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
  public ResponseEntity<?> createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam,
      BindingResult bindingResult,
      @AuthenticationPrincipal String userId) {
    if (bindingResult.hasErrors()) {
      throw new InvalidRequestException(bindingResult);
    }

    Article article = articleCommandService.createArticle(newArticleParam, userId);
    return ResponseEntity.ok(
        articleResponse(articleQueryService.findById(article.getId(), userId).get()));
  }

  @GetMapping(path = "feed")
  public ResponseEntity<?> getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(articleQueryService.findUserFeed(userId, new Page(offset, limit)));
  }

  @GetMapping
  public ResponseEntity<ArticleDataList> getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(tag, author, favoritedBy, new Page(offset, limit), userId));
  }

  private Map<String, Object> articleResponse(ArticleData articleData) {
    return new HashMap<String, Object>() {
      {
        put("article", articleData);
      }
    };
  }
}
