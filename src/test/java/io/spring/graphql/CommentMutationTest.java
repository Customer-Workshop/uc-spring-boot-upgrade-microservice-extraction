package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
// Auth cleared context causes NPE in SecurityUtil
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentMutation commentMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    article = new Article("Test Article", "desc", "body", Arrays.asList("java"), user.getId());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_success() {
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));

    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("test-article", "comment body");

    assertNotNull(result);
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_create_comment_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    assertThrows(
        NullPointerException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_create_comment_throw_when_article_not_found() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void should_delete_comment_success() {
    Comment comment = new Comment("comment body", user.getId(), article.getId());
    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus status = commentMutation.removeComment("test-article", comment.getId());

    assertTrue(status.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_delete_comment_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    assertThrows(
        NullPointerException.class, () -> commentMutation.removeComment("slug", "commentId"));
  }

  @Test
  void should_delete_comment_throw_when_article_not_found() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "commentId"));
  }

  @Test
  void should_delete_comment_throw_when_comment_not_found() {
    when(articleRepository.findBySlug("test-article")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-article", "missing"));
  }

  @Test
  void should_delete_comment_throw_when_not_authorized() {
    User otherUser = new User("other@example.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());
    Comment comment = new Comment("comment body", otherUser.getId(), otherArticle.getId());

    when(articleRepository.findBySlug("other-article")).thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(otherArticle.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("other-article", comment.getId()));
  }
}
