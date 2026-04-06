package io.spring.article.api;

import io.spring.article.application.ArticleQueryService;
import io.spring.article.application.Page;
import io.spring.article.application.article.ArticleCommandService;
import io.spring.article.application.article.NewArticleParam;
import io.spring.article.client.UserServiceClient;
import io.spring.article.core.Article;
import java.util.HashMap;
import java.util.List;
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
  private UserServiceClient userServiceClient;

  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam,
      @AuthenticationPrincipal String userId) {
    Article article = articleCommandService.createArticle(newArticleParam, userId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleQueryService.findById(article.getId()).get());
          }
        });
  }

  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal String userId) {
    List<String> followedUsers = userServiceClient.getFollowedUsers(userId);
    return ResponseEntity.ok(
        articleQueryService.findArticlesOfAuthors(followedUsers, new Page(offset, limit)));
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(tag, author, favoritedBy, new Page(offset, limit)));
  }
}
