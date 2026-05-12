package io.spring.api;

import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles")
@AllArgsConstructor
public class ArticleBookmarkApi {
  private ArticleBookmarkRepository articleBookmarkRepository;
  private ArticleRepository articleRepository;
  private ArticleQueryService articleQueryService;

  @PostMapping(path = "/{slug}/bookmark")
  public ResponseEntity bookmarkArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    ArticleBookmark bookmark = new ArticleBookmark(article.getId(), user.getId());
    articleBookmarkRepository.save(bookmark);
    return responseArticleData(articleQueryService.findBySlug(slug, user).get());
  }

  @DeleteMapping(path = "/{slug}/bookmark")
  public ResponseEntity unbookmarkArticle(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    articleBookmarkRepository
        .find(article.getId(), user.getId())
        .ifPresent(bookmark -> articleBookmarkRepository.remove(bookmark));
    return responseArticleData(articleQueryService.findBySlug(slug, user).get());
  }

  @GetMapping(path = "/bookmarks")
  public ResponseEntity getBookmarkedArticles(@AuthenticationPrincipal User user) {
    List<String> articleIds = articleBookmarkRepository.findBookmarkedArticleIds(user.getId());
    List<ArticleData> articles =
        articleIds.stream()
            .map(id -> articleQueryService.findById(id, user).orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("articles", articles);
            put("articlesCount", articles.size());
          }
        });
  }

  private ResponseEntity<HashMap<String, Object>> responseArticleData(
      final ArticleData articleData) {
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("article", articleData);
          }
        });
  }
}
