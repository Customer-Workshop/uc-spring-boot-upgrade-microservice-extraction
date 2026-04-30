package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;
  private User user;
  private Article article;
  private MockedStatic<SecurityUtil> securityUtilMock;

  @BeforeEach
  void setUp() {
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    article = new Article("Test Title", "Desc", "Body", Arrays.asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    if (securityUtilMock != null) {
      securityUtilMock.close();
    }
  }

  private void mockAuthenticated() {
    securityUtilMock = mockStatic(SecurityUtil.class);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
  }

  private void mockUnauthenticated() {
    securityUtilMock = mockStatic(SecurityUtil.class);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
  }

  @Test
  void should_create_comment_successfully() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Comment body",
            article.getId(),
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-slug", "Comment body");

    assertNotNull(result);
    assertNotNull(result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_when_create_comment_unauthenticated() {
    mockUnauthenticated();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("test-slug", "body"));
  }

  @Test
  void should_throw_when_create_comment_article_not_found() {
    mockAuthenticated();
    when(articleRepository.findBySlug("unknown")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("unknown", "body"));
  }

  @Test
  void should_throw_when_created_comment_not_found() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("test-slug", "body"));
  }

  @Test
  void should_delete_comment_successfully() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), "comment-id"))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-slug", "comment-id");

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_when_delete_comment_unauthenticated() {
    mockUnauthenticated();
    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("test-slug", "comment-id"));
  }

  @Test
  void should_throw_when_delete_comment_article_not_found() {
    mockAuthenticated();
    when(articleRepository.findBySlug("unknown")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("unknown", "comment-id"));
  }

  @Test
  void should_throw_when_delete_comment_not_found() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "unknown")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-slug", "unknown"));
  }

  @Test
  void should_throw_when_delete_comment_not_authorized() {
    User otherUser = new User("other@test.com", "other", "123", "", "");
    Article otherArticle =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), otherUser.getId());

    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(otherArticle));

    Comment comment = new Comment("body", otherUser.getId(), otherArticle.getId());
    when(commentRepository.findById(otherArticle.getId(), "comment-id"))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("test-slug", "comment-id"));
  }
}
