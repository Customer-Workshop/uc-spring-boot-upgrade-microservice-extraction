package io.spring.comments.api;

import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
import io.spring.comments.infrastructure.mybatis.readservice.CommentReadService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CommentsApi {
  private CommentRepository commentRepository;
  private CommentReadService commentReadService;

  @PostMapping("/api/articles/{articleId}/comments")
  public ResponseEntity<?> createComment(
      @PathVariable("articleId") String articleId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Comment comment =
        new Comment(newCommentParam.getBody(), newCommentParam.getUserId(), articleId);
    commentRepository.save(comment);
    CommentData commentData = commentReadService.findById(comment.getId());
    return ResponseEntity.status(201).body(wrapComment(commentData));
  }

  @GetMapping("/api/articles/{articleId}/comments")
  public ResponseEntity<?> getComments(@PathVariable("articleId") String articleId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @GetMapping("/api/articles/{articleId}/comments/{commentId}")
  public ResponseEntity<?> getComment(
      @PathVariable("articleId") String articleId, @PathVariable("commentId") String commentId) {
    return commentRepository
        .findById(articleId, commentId)
        .map(
            comment -> {
              CommentData commentData = commentReadService.findById(commentId);
              return ResponseEntity.ok(wrapComment(commentData));
            })
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/api/comments/{commentId}")
  public ResponseEntity<?> getCommentById(@PathVariable("commentId") String commentId) {
    CommentData commentData = commentReadService.findById(commentId);
    if (commentData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(wrapComment(commentData));
  }

  @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("articleId") String articleId, @PathVariable("commentId") String commentId) {
    return commentRepository
        .findById(articleId, commentId)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private Map<String, Object> wrapComment(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}

@Getter
@NoArgsConstructor
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;

  private String userId;
}
