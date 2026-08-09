package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CommentMutationTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();
  private final Article article = GraphqlTestFixtures.article(user);
  private final io.spring.core.comment.Comment comment = GraphqlTestFixtures.comment(article, user);
  private final CommentData data = GraphqlTestFixtures.commentData(comment, article, user);

  @Test
  void createsCommentForAuthenticatedUser() {
    ArticleRepository articles = mock(ArticleRepository.class);
    CommentRepository comments = mock(CommentRepository.class);
    CommentQueryService queries = mock(CommentQueryService.class);
    when(articles.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(queries.findById(any(), eq(user))).thenReturn(Optional.of(data));
    CommentMutation mutation = new CommentMutation(articles, comments, queries);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertTrue(mutation.createComment(article.getSlug(), "body").getLocalContext() == data);
      verify(comments).save(any());
    }
  }

  @Test
  void rejectsUnauthenticatedAndMissingArticle() {
    CommentMutation unauthenticatedMutation =
        new CommentMutation(
            mock(ArticleRepository.class),
            mock(CommentRepository.class),
            mock(CommentQueryService.class));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(
          AuthenticationException.class,
          () -> unauthenticatedMutation.createComment("missing", "body"));
    }
    ArticleRepository articles = mock(ArticleRepository.class);
    when(articles.findBySlug("missing")).thenReturn(Optional.empty());
    CommentMutation missingArticleMutation =
        new CommentMutation(
            articles, mock(CommentRepository.class), mock(CommentQueryService.class));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(
          ResourceNotFoundException.class,
          () -> missingArticleMutation.createComment("missing", "body"));
    }
  }

  @Test
  void deletesOwnedCommentAndRejectsOtherOwner() {
    ArticleRepository articles = mock(ArticleRepository.class);
    CommentRepository comments = mock(CommentRepository.class);
    when(articles.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(comments.findById(article.getId(), comment.getId())).thenReturn(Optional.of(comment));
    CommentMutation mutation =
        new CommentMutation(articles, comments, mock(CommentQueryService.class));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertTrue(mutation.removeComment(article.getSlug(), comment.getId()).getSuccess());
      verify(comments).remove(comment);
    }
    User other = GraphqlTestFixtures.user();
    Article otherArticle = GraphqlTestFixtures.article(other);
    io.spring.core.comment.Comment otherComment = GraphqlTestFixtures.comment(otherArticle, other);
    when(articles.findBySlug(otherArticle.getSlug())).thenReturn(Optional.of(otherArticle));
    when(comments.findById(otherArticle.getId(), otherComment.getId()))
        .thenReturn(Optional.of(otherComment));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(
          NoAuthorizationException.class,
          () -> mutation.removeComment(otherArticle.getSlug(), otherComment.getId()));
      assertThrows(
          ResourceNotFoundException.class,
          () -> mutation.removeComment(article.getSlug(), "missing"));
    }
  }
}
