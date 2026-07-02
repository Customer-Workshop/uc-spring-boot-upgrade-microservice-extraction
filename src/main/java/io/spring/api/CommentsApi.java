package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.AuthorizationService;
import io.spring.core.user.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for comment operations on articles.
 *
 * <p>Handles creating, listing, and deleting comments attached to a specific article.
 */
@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;

  /**
   * Creates a new comment on an article.
   *
   * @param slug the URL-friendly article identifier
   * @param user the currently authenticated user
   * @param newCommentParam the comment body
   * @return 201 Created with the comment wrapped in a {@code {"comment": ...}} envelope
   * @throws ResourceNotFoundException if no article exists with the given slug
   */
  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    Comment comment = new Comment(newCommentParam.getBody(), user.getId(), article.getId());
    commentRepository.save(comment);
    return ResponseEntity.status(201)
        .body(commentResponse(commentQueryService.findById(comment.getId(), user).get()));
  }

  /**
   * Lists all comments for an article.
   *
   * @param slug the URL-friendly article identifier
   * @param user the currently authenticated user, or {@code null} if anonymous
   * @return the list of comments wrapped in a {@code {"comments": [...]}} envelope
   * @throws ResourceNotFoundException if no article exists with the given slug
   */
  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    List<CommentData> comments = commentQueryService.findByArticleId(article.getId(), user);
    return ResponseEntity.ok(
        new HashMap<String, Object>() {
          {
            put("comments", comments);
          }
        });
  }

  /**
   * Deletes a comment from an article.
   *
   * @param slug the URL-friendly article identifier
   * @param commentId the unique comment identifier
   * @param user the currently authenticated user
   * @return 204 No Content on success
   * @throws ResourceNotFoundException if the article or comment does not exist
   * @throws NoAuthorizationException if the user is neither the article author nor the comment
   *     author
   */
  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentRepository
        .findById(article.getId(), commentId)
        .map(
            comment -> {
              if (!AuthorizationService.canWriteComment(user, article, comment)) {
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

/** Request payload DTO for creating a new comment. Deserialized from the {@code "comment"} root. */
@Getter
@NoArgsConstructor
@JsonRootName("comment")
class NewCommentParam {
  @NotBlank(message = "can't be empty")
  private String body;
}
