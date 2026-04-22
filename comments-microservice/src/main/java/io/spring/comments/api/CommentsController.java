package io.spring.comments.api;

import io.spring.comments.application.data.CommentData;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/articles/{articleId}/comments")
@AllArgsConstructor
public class CommentsController {
  private CommentRepository commentRepository;
  private CommentReadService commentReadService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("articleId") String articleId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Comment comment =
        new Comment(newCommentParam.getBody(), newCommentParam.getUserId(), articleId);
    commentRepository.save(comment);
    CommentData commentData = commentReadService.findById(comment.getId());
    return ResponseEntity.status(201).body(commentResponse(commentData));
  }

  @GetMapping
  public ResponseEntity<?> getComments(@PathVariable("articleId") String articleId) {
    List<CommentData> comments = commentReadService.findByArticleId(articleId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getComment(
      @PathVariable("articleId") String articleId, @PathVariable("id") String id) {
    CommentData commentData = commentReadService.findById(id);
    if (commentData == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(commentResponse(commentData));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("articleId") String articleId, @PathVariable("id") String id) {
    return commentRepository
        .findById(articleId, id)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }

  @GetMapping("/health")
  public ResponseEntity<?> health() {
    return ResponseEntity.ok(Map.of("status", "UP"));
  }
}

@Getter
@NoArgsConstructor
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;

  private String userId;
}
