package io.spring.article.api;

import io.spring.article.api.exception.NoAuthorizationException;
import io.spring.article.api.exception.ResourceNotFoundException;
import io.spring.article.application.CommentQueryService;
import io.spring.article.application.data.CommentData;
import io.spring.article.core.article.Article;
import io.spring.article.core.article.ArticleRepository;
import io.spring.article.core.comment.Comment;
import io.spring.article.core.comment.CommentRepository;
import io.spring.article.core.service.AuthorizationService;
import io.spring.article.infrastructure.client.UserProfileCacheService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;
  private UserProfileCacheService userProfileCacheService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal String userId,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    userProfileCacheService.ensureProfileCached(userId);
    Comment comment = new Comment(newCommentParam.getBody(), userId, article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(
            commentQueryService.findById(comment.getId(), userId).orElseThrow(ResourceNotFoundException::new)));
  }

  @GetMapping
  public ResponseEntity<?> getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), userId);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  @DeleteMapping(path = "{id}")
  public ResponseEntity<?> deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal String userId) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(userId, article, comment)) {
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

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
