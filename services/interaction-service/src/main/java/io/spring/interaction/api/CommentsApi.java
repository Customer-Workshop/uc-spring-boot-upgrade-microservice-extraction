package io.spring.interaction.api;

import io.spring.interaction.api.exception.NoAuthorizationException;
import io.spring.interaction.api.exception.ResourceNotFoundException;
import io.spring.interaction.application.CommentQueryService;
import io.spring.interaction.application.data.CommentData;
import io.spring.interaction.client.ArticleServiceClient;
import io.spring.interaction.core.Comment;
import io.spring.interaction.core.CommentRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;
  private ArticleServiceClient articleServiceClient;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    String articleId = articleServiceClient.getArticleId(slug);
    if (articleId == null) {
      throw new ResourceNotFoundException();
    }
    Comment comment = new Comment(newCommentParam.getBody(), userId, articleId);
    commentRepository.save(comment);
    return commentQueryService
        .findById(comment.getId(), userId)
        .map(commentData -> ResponseEntity.ok(commentResponse(commentData)))
        .orElseThrow(ResourceNotFoundException::new);
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    String articleId = articleServiceClient.getArticleId(slug);
    if (articleId == null) {
      throw new ResourceNotFoundException();
    }
    List<CommentData> comments = commentQueryService.findByArticleId(articleId, userId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal String userId) {
    String articleId = articleServiceClient.getArticleId(slug);
    if (articleId == null) {
      throw new ResourceNotFoundException();
    }
    return commentRepository
        .findById(articleId, commentId)
        .map(
            comment -> {
              if (!userId.equals(comment.getUserId())) {
                throw new NoAuthorizationException();
              }
              commentRepository.remove(comment);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private Map<String, Object> commentResponse(CommentData commentData) {
    return new HashMap<String, Object>() {
      {
        put("comment", commentData);
      }
    };
  }
}
