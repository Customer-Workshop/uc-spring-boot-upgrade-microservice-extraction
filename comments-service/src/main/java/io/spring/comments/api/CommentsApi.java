package io.spring.comments.api;

import io.spring.comments.application.CommentQueryService;
import io.spring.comments.application.data.CommentData;
import io.spring.comments.core.Comment;
import io.spring.comments.core.CommentRepository;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/comments")
public class CommentsApi {
  private final CommentRepository commentRepository;
  private final CommentQueryService commentQueryService;

  public CommentsApi(CommentRepository commentRepository, CommentQueryService commentQueryService) {
    this.commentRepository = commentRepository;
    this.commentQueryService = commentQueryService;
  }

  @PostMapping
  public ResponseEntity<?> createComment(@Valid @RequestBody NewCommentParam newCommentParam) {
    Comment comment =
        new Comment(
            newCommentParam.getBody(), newCommentParam.getUserId(), newCommentParam.getArticleId());
    commentRepository.save(comment);
    return commentQueryService
        .findById(comment.getId(), null)
        .map(commentData -> ResponseEntity.status(201).body(commentResponse(commentData)))
        .orElse(ResponseEntity.status(201).build());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getComment(
      @PathVariable("id") String id,
      @RequestParam(value = "articleId", required = true) String articleId) {
    return commentRepository
        .findById(articleId, id)
        .map(
            comment ->
                commentQueryService
                    .findById(id, null)
                    .map(commentData -> ResponseEntity.ok(commentResponse(commentData)))
                    .orElse(ResponseEntity.notFound().build()))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<?> getComments(
      @RequestParam(value = "articleId", required = true) String articleId) {
    List<CommentData> comments = commentQueryService.findByArticleId(articleId, null);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteComment(@PathVariable("id") String id) {
    commentRepository.remove(new CommentHolder(id));
    return ResponseEntity.noContent().build();
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }

  private static class CommentHolder extends Comment {
    private final String id;

    CommentHolder(String id) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }
  }
}

@Getter
@NoArgsConstructor
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;

  private String userId;
  private String articleId;
}
