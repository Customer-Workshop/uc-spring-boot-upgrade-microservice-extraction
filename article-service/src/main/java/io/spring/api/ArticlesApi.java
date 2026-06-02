package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.core.article.Article;
import io.spring.core.service.UserDto;
import io.spring.core.service.UserServiceClient;
import java.util.HashMap;
import javax.validation.Valid;
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
public class ArticlesApi {
  private ArticleCommandService articleCommandService;
  private ArticleQueryService articleQueryService;
  private UserServiceClient userServiceClient;

  public ArticlesApi(
      ArticleCommandService articleCommandService,
      ArticleQueryService articleQueryService,
      UserServiceClient userServiceClient) {
    this.articleCommandService = articleCommandService;
    this.articleQueryService = articleQueryService;
    this.userServiceClient = userServiceClient;
  }

  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal UserDto user) {
    Article article = articleCommandService.createArticle(newArticleParam, user.getId());
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleQueryService.findById(article.getId()).get());
          }
        });
  }

  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal UserDto user) {
    String authorUserId = resolveAuthorToUserId(author);
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(tag, authorUserId, new Page(offset, limit)));
  }

  private String resolveAuthorToUserId(String author) {
    if (author == null) {
      return null;
    }
    return userServiceClient.findUserByUsername(author).map(UserDto::getId).orElse(author);
  }
}
