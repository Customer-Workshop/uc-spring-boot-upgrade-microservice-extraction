package io.spring.api;

import com.fasterxml.jackson.annotation.JsonRootName;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.CommentsServiceClient;
import io.spring.infrastructure.service.CommentsServiceClient.CommentResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
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
  private CommentsServiceClient commentsServiceClient;
  private UserReadService userReadService;

  @PostMapping
  public ResponseEntity<?> createComment(
      @PathVariable("slug") String slug,
      @AuthenticationPrincipal User user,
      @Valid @RequestBody NewCommentParam newCommentParam) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    CommentResponse response =
        commentsServiceClient.createComment(
            article.getId(), newCommentParam.getBody(), user.getId());
    if (response == null) {
      throw new ResourceNotFoundException();
    }
    CommentData commentData = toCommentData(response);
    return ResponseEntity.status(201).body(commentResponse(commentData));
  }

  @GetMapping
  public ResponseEntity getComments(
      @PathVariable("slug") String slug, @AuthenticationPrincipal User user) {
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    List<CommentResponse> responses =
        commentsServiceClient.getCommentsByArticleId(article.getId());
    List<CommentData> comments =
        responses.stream().map(this::toCommentData).collect(Collectors.toList());
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
    return commentsServiceClient
        .getComment(article.getId(), commentId)
        .map(
            commentResponse -> {
              if (!user.getId().equals(article.getUserId())
                  && !user.getId().equals(commentResponse.getUserId())) {
                throw new NoAuthorizationException();
              }
              commentsServiceClient.deleteComment(article.getId(), commentId);
              return ResponseEntity.noContent().build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private CommentData toCommentData(CommentResponse response) {
    CommentData data = new CommentData();
    data.setId(response.getId());
    data.setBody(response.getBody());
    data.setArticleId(response.getArticleId());
    data.setCreatedAt(response.getCreatedAt() != null ? response.getCreatedAt() : new DateTime());
    data.setUpdatedAt(response.getUpdatedAt() != null ? response.getUpdatedAt() : new DateTime());
    if (response.getUserId() != null) {
      UserData userData = userReadService.findById(response.getUserId());
      if (userData != null) {
        data.setProfileData(
            new ProfileData(
                userData.getId(),
                userData.getUsername(),
                userData.getBio(),
                userData.getImage(),
                false));
      }
    }
    return data;
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
