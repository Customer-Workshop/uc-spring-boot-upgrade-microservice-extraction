package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.application.data.UserData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceResponse;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@DgsComponent
@AllArgsConstructor
public class CommentMutation {

  private ArticleRepository articleRepository;
  private CommentServiceClient commentServiceClient;
  private UserReadService userReadService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  public DataFetcherResult<CommentPayload> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    CommentServiceResponse response =
        commentServiceClient.createComment(article.getId(), body, user.getId());
    CommentData commentData = toCommentData(response, user);
    return DataFetcherResult.<CommentPayload>newResult()
        .localContext(commentData)
        .data(CommentPayload.newBuilder().build())
        .build();
  }

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.DeleteComment)
  public DeletionStatus removeComment(
      @InputArgument("slug") String slug, @InputArgument("id") String commentId) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);

    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    return commentServiceClient
        .getComment(article.getId(), commentId)
        .map(
            response -> {
              if (!user.getId().equals(article.getUserId())
                  && !user.getId().equals(response.getUserId())) {
                throw new NoAuthorizationException();
              }
              commentServiceClient.deleteComment(article.getId(), commentId);
              return DeletionStatus.newBuilder().success(true).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }

  private CommentData toCommentData(CommentServiceResponse response, User user) {
    UserData userData = userReadService.findById(response.getUserId());
    ProfileData profileData;
    if (userData != null) {
      profileData =
          new ProfileData(
              userData.getId(),
              userData.getUsername(),
              userData.getBio(),
              userData.getImage(),
              false);
    } else {
      profileData = new ProfileData(response.getUserId(), "", "", "", false);
    }
    DateTime createdAt =
        response.getCreatedAt() != null ? DateTime.parse(response.getCreatedAt()) : new DateTime();
    return new CommentData(
        response.getId(), response.getBody(), response.getArticleId(), createdAt, createdAt,
        profileData);
  }
}
