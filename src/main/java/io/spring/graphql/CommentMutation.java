package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentEnrichmentService;
import io.spring.application.data.CommentData;
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

@DgsComponent
@AllArgsConstructor
public class CommentMutation {

  private ArticleRepository articleRepository;
  private CommentServiceClient commentServiceClient;
  private CommentEnrichmentService commentEnrichmentService;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  public DataFetcherResult<CommentPayload> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    CommentResponse created =
        commentServiceClient.createComment(body, user.getId(), article.getId());
    CommentData commentData =
        commentEnrichmentService
            .findById(created.getId(), user)
            .orElseThrow(ResourceNotFoundException::new);
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
    return commentEnrichmentService
        .findByIdAndArticleId(commentId, article.getId(), user)
        .map(
            commentData -> {
              if (!user.getId().equals(article.getUserId())
                  && !user.getId().equals(commentData.getProfileData().getId())) {
                throw new NoAuthorizationException();
              }
              commentServiceClient.deleteComment(commentId);
              return DeletionStatus.newBuilder().success(true).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
