package io.spring.api;

import io.spring.application.ArticleQueryService;
import io.spring.application.Page;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.core.article.Article;
import io.spring.core.user.User;
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

/**
 * REST controller for article collection operations.
 *
 * <p>Handles creating new articles, listing articles with filters, and retrieving the authenticated
 * user's personalized feed.
 */
@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticlesApi {
  private ArticleCommandService articleCommandService;
  private ArticleQueryService articleQueryService;

  /**
   * Creates a new article.
   *
   * @param newArticleParam the article creation payload (title, description, body, tagList)
   * @param user the currently authenticated user who will be the article author
   * @return the created article wrapped in an {@code {"article": ...}} envelope
   */
  @PostMapping
  public ResponseEntity createArticle(
      @Valid @RequestBody NewArticleParam newArticleParam, @AuthenticationPrincipal User user) {
    Article article = articleCommandService.createArticle(newArticleParam, user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleQueryService.findById(article.getId(), user).get());
          }
        });
  }

  /**
   * Retrieves the authenticated user's personalized feed of articles from followed authors.
   *
   * @param offset zero-based pagination offset (default 0)
   * @param limit maximum number of articles to return (default 20)
   * @param user the currently authenticated user
   * @return a paginated list of articles with total count
   */
  @GetMapping(path = "feed")
  public ResponseEntity getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(articleQueryService.findUserFeed(user, new Page(offset, limit)));
  }

  /**
   * Lists recent articles, optionally filtered by tag, author, or favorited-by user.
   *
   * @param offset zero-based pagination offset (default 0)
   * @param limit maximum number of articles to return (default 20)
   * @param tag filter articles that have this tag
   * @param favoritedBy filter articles favorited by this username
   * @param author filter articles written by this username
   * @param user the currently authenticated user, or {@code null} if anonymous
   * @return a paginated list of articles with total count
   */
  @GetMapping
  public ResponseEntity getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        articleQueryService.findRecentArticles(
            tag, author, favoritedBy, new Page(offset, limit), user));
  }
}
