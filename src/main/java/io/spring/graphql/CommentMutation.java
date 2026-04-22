package io.spring.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.InputArgument;
import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.graphql.DgsConstants.MUTATION;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import io.spring.infrastructure.service.CommentsServiceClient;
import io.spring.infrastructure.service.CommentsServiceClient.CommentResponse;
import lombok.AllArgsConstructor;
import org.joda.time.DateTime;

@DgsComponent
@AllArgsConstructor
public class CommentMutation {

  private ArticleRepository articleRepository;
  private CommentsServiceClient commentsServiceClient;

  @DgsData(parentType = MUTATION.TYPE_NAME, field = MUTATION.AddComment)
  public DataFetcherResult<CommentPayload> createComment(
      @InputArgument("slug") String slug, @InputArgument("body") String body) {
    User user = SecurityUtil.getCurrentUser().orElseThrow(AuthenticationException::new);
    Article article =
        articleRepository.findBySlug(slug).orElseThrow(ResourceNotFoundException::new);
    CommentResponse response =
        commentsServiceClient.createComment(article.getId(), body, user.getId());
    if (response == null) {
      throw new ResourceNotFoundException();
    }
    CommentData commentData = new CommentData();
    commentData.setId(response.getId());
    commentData.setBody(response.getBody());
    commentData.setArticleId(response.getArticleId());
    commentData.setCreatedAt(
        response.getCreatedAt() != null ? response.getCreatedAt() : new DateTime());
    commentData.setUpdatedAt(
        response.getUpdatedAt() != null ? response.getUpdatedAt() : new DateTime());
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
    return commentsServiceClient
        .getComment(article.getId(), commentId)
        .map(
            commentResponse -> {
              if (!user.getId().equals(article.getUserId())
                  && !user.getId().equals(commentResponse.getUserId())) {
                throw new NoAuthorizationException();
              }
              commentsServiceClient.deleteComment(article.getId(), commentId);
              return DeletionStatus.newBuilder().success(true).build();
            })
        .orElseThrow(ResourceNotFoundException::new);
  }
}
