package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceResponse;
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

@RestController
@RequestMapping(path = "/articles/{slug}/comments")
@AllArgsConstructor
public class CommentsApi {
  private ArticleRepository articleRepository;
  private CommentQueryService commentQueryService;
  private CommentServiceClient commentServiceClient;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    CommentServiceResponse created =
        commentServiceClient.createComment(
            newCommentParam.getBody(), user.getId(), article.getId());
    CommentData commentData = commentQueryService.findById(created.getId(), user).orElse(null);
    if (commentData != null) {
      return ResponseEntity.status(201).body(commentResponse(commentData));
    }
    return ResponseEntity.status(201)
        .body(
            commentResponse(
                new CommentData(
                    created.getId(),
                    created.getBody(),
                    created.getArticleId(),
                    null,
                    null,
                    new ProfileData(
                        user.getId(),
                        user.getUsername(),
                        user.getBio(),
                        user.getImage(),
                        false))));
  }

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

  @RequestMapping(path = "{id}", method = RequestMethod.DELETE)
  public ResponseEntity deleteComment(
      @PathVariable("slug") String slug,
      @PathVariable("id") String commentId,
      @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);

    CommentServiceResponse commentResp =
        commentServiceClient.getComment(commentId, article.getId());
    if (commentResp == null) {
      throw new ResourceNotFoundException();
    }

    boolean isArticleAuthor = user.getId().equals(article.getUserId());
    boolean isCommentAuthor = user.getId().equals(commentResp.getUserId());
    if (!isArticleAuthor && !isCommentAuthor) {
      throw new NoAuthorizationException();
    }

    commentServiceClient.deleteComment(commentId, article.getId());
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
