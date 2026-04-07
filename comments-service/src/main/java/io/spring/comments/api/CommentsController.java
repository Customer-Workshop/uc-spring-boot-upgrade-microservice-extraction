package io.spring.comments.api;

import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("articleId") String articleId,
      @Valid @RequestBody NewCommentRequest newCommentRequest) {
    Comment comment =
        new Comment(newCommentRequest.getBody(), newCommentRequest.getUserId(), articleId);
    commentRepository.save(comment);
    return ResponseEntity.status(201).body(commentResponse(toCommentData(comment)));
  }

  @GetMapping
  public ResponseEntity<?> getComments(@PathVariable("articleId") String articleId) {
    List<Comment> comments = commentRepository.findByArticleId(articleId);
    List<CommentData> commentDataList =
        comments.stream().map(this::toCommentData).collect(Collectors.toList());
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", commentDataList);
          }
        });
  }

  @GetMapping("/{commentId}")
  public ResponseEntity<?> getComment(
      @PathVariable("articleId") String articleId,
      @PathVariable("commentId") String commentId) {
    return commentRepository
        .findByArticleIdAndId(articleId, commentId)
        .map(comment -> ResponseEntity.ok(commentResponse(toCommentData(comment))))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("articleId") String articleId,
      @PathVariable("commentId") String commentId) {
    return commentRepository
        .findByArticleIdAndId(articleId, commentId)
        .map(
            comment -> {
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElse(ResponseEntity.notFound().build());
  }

  private CommentData toCommentData(Comment comment) {
    return new CommentData(
        comment.getId(),
        comment.getBody(),
        comment.getArticleId(),
        comment.getUserId(),
        comment.getCreatedAt(),
        comment.getCreatedAt());
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
class NewCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  private String userId;
}
