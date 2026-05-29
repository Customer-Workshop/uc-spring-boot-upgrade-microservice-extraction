package io.spring.articleservice.controller;

import io.spring.articleservice.dto.ArticleDto;
import io.spring.articleservice.dto.ArticleListDto;
import io.spring.articleservice.dto.request.NewArticleRequest;
import io.spring.articleservice.service.ArticleService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticlesController {

  private final ArticleService articleService;

  @PostMapping
  public ResponseEntity<Map<String, ArticleDto>> createArticle(
      @Valid @RequestBody Map<String, NewArticleRequest> wrapper,
      @AuthenticationPrincipal String userId) {
    NewArticleRequest request = wrapper.get("article");
    ArticleDto article = articleService.createArticle(request, userId);
    return ResponseEntity.ok(Map.of("article", article));
  }

  @GetMapping("/feed")
  public ResponseEntity<ArticleListDto> getFeed(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(articleService.getUserFeed(userId, offset, limit));
  }

  @GetMapping
  public ResponseEntity<ArticleListDto> getArticles(
      @RequestParam(value = "offset", defaultValue = "0") int offset,
      @RequestParam(value = "limit", defaultValue = "20") int limit,
      @RequestParam(value = "tag", required = false) String tag,
      @RequestParam(value = "favorited", required = false) String favoritedBy,
      @RequestParam(value = "author", required = false) String author,
      @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(
        articleService.listArticles(tag, author, favoritedBy, offset, limit, userId));
  }
}
