package io.spring.commentservice.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.commentservice.application.CommentService;
import io.spring.commentservice.application.dto.ArticleDto;
import io.spring.commentservice.application.dto.CommentData;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.infrastructure.client.MonolithClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
public class CommentsController {
  private final CommentService commentService;
  private final MonolithClient monolithClient;

  public CommentsController(CommentService commentService, MonolithClient monolithClient) {
    this.commentService = commentService;
    this.monolithClient = monolithClient;
  }

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @RequestHeader(value = "X-User-Id", required = false) String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    ArticleDto article = monolithClient.getArticleBySlug(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    Comment comment =
        commentService.createComment(newCommentParam.getBody(), userId, article.getId());
    CommentData commentData = commentService.findById(comment.getId()).orElse(null);
    return ResponseEntity.status(201).body(commentResponse(commentData));
  }

  @GetMapping
  public ResponseEntity<?> getComments(@PathVariable("slug") String slug) {
    ArticleDto article = monolithClient.getArticleBySlug(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    List<CommentData> comments = commentService.findByArticleId(article.getId());
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("slug") String slug, @PathVariable("id") String commentId) {
    ArticleDto article = monolithClient.getArticleBySlug(slug);
    if (article == null) {
      return ResponseEntity.notFound().build();
    }
    commentService.deleteComment(commentId);
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
