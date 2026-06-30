package io.spring.comments.api;

import io.spring.comments.application.data.CommentData;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/comments")
@AllArgsConstructor
public class CommentsApi {
  private CommentRepository commentRepository;

  @PostMapping
  public ResponseEntity<?> createComment(@Valid @RequestBody CreateCommentRequest request) {
    Comment comment = new Comment(request.getBody(), request.getUserId(), request.getArticleId());
    commentRepository.save(comment);
    CommentData data = toCommentData(comment);
    return ResponseEntity.status(201).body(wrapComment(data));
  }

  @GetMapping
  public ResponseEntity<?> getCommentsByArticleId(@RequestParam("articleId") String articleId) {
    List<Comment> comments = commentRepository.findByArticleId(articleId);
    List<CommentData> commentDataList =
        comments.stream().map(this::toCommentData).collect(Collectors.toList());
    Map<String, Object> response = new HashMap<>();
    response.put("comments", commentDataList);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getCommentById(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = false) String articleId) {
    if (articleId != null) {
      return commentRepository
          .findById(articleId, id)
          .map(comment -> ResponseEntity.ok(wrapComment(toCommentData(comment))))
          .orElse(ResponseEntity.notFound().build());
    }
    return commentRepository
        .findCommentById(id)
        .map(comment -> ResponseEntity.ok(wrapComment(toCommentData(comment))))
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteComment(@PathVariable("id") String id) {
    return commentRepository
        .findCommentById(id)
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

  private Map<String, Object> wrapComment(CommentData data) {
    Map<String, Object> response = new HashMap<>();
    response.put("comment", data);
    return response;
  }
}

@Getter
@NoArgsConstructor
class CreateCommentRequest {
  @NotBlank(message = "can't be empty")
  private String body;

  @NotBlank(message = "can't be empty")
  private String userId;

  @NotBlank(message = "can't be empty")
  private String articleId;
}
