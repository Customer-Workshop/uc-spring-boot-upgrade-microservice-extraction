package io.spring.article.api;

import io.spring.article.api.dto.CommentResponse;
import io.spring.article.api.dto.CreateCommentRequest;
import io.spring.article.domain.Article;
import io.spring.article.domain.Comment;
import io.spring.article.service.ArticleService;
import io.spring.article.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles/{slug}/comments")
@AllArgsConstructor
public class CommentController {
  private CommentService commentService;
  private ArticleService articleService;

  @PostMapping
  public ResponseEntity<CommentResponse> createComment(
      @PathVariable("slug") String slug,
      @Valid @RequestBody CreateCommentRequest request) {
    Optional<Article> article = articleService.findBySlug(slug);
    if (article.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Comment comment =
        commentService.createComment(request.getBody(), request.getUserId(), article.get().getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
  }

  @GetMapping
  public ResponseEntity<List<CommentResponse>> getComments(@PathVariable("slug") String slug) {
    Optional<Article> article = articleService.findBySlug(slug);
    if (article.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    List<CommentResponse> comments =
        commentService.findByArticleId(article.get().getId()).stream()
            .map(CommentResponse::from)
            .collect(Collectors.toList());
    return ResponseEntity.ok(comments);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable("slug") String slug, @PathVariable("id") String id) {
    Optional<Article> article = articleService.findBySlug(slug);
    if (article.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    boolean deleted = commentService.deleteComment(article.get().getId(), id);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.noContent().build();
  }
}
