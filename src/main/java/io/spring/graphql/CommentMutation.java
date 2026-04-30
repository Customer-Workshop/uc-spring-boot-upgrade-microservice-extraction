package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@DgsComponent
@AllArgsConstructor
public class CommentMutation {

  private ArticleRepository articleRepository;
  private CommentQueryService commentQueryService;
  private CommentServiceClient commentServiceClient;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  public DataFetcherResult<CommentPayload> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    CommentResponse created =
        commentServiceClient.createComment(article.getId(), body, user.getId());
    CommentData commentData =
        commentQueryService
            .findById(created.getId(), user)
            .orElseGet(
                () -> {
                  ProfileData profileData =
                      new ProfileData(
                          user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
                  return new CommentData(
                      created.getId(),
                      created.getBody(),
                      created.getArticleId(),
                      new DateTime(),
                      new DateTime(),
                      profileData);
                });
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
    CommentResponse comment =
        commentServiceClient
            .getComment(article.getId(), commentId)
            .orElseThrow(ResourceNotFoundException::new);
    if (!user.getId().equals(article.getUserId()) && !user.getId().equals(comment.getUserId())) {
      throw new NoAuthorizationException();
    }
    commentServiceClient.deleteComment(article.getId(), commentId);
    return DeletionStatus.newBuilder().success(true).build();
  }
}
