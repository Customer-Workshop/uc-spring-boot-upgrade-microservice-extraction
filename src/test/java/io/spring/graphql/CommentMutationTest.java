package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  @Test
  void should_create_comment() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Great article!",
            article.getId(),
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "Great article!");

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_create_comment_throw_when_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_create_comment_throw_when_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("missing", "body"));
  }

  @Test
  void should_delete_comment() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", comment.getId());

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_delete_comment_throw_when_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment("slug", "comment-id"));
  }

  @Test
  void should_delete_comment_throw_when_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("missing", "comment-id"));
  }

  @Test
  void should_delete_comment_throw_when_comment_not_found() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("title", "missing"));
  }

  @Test
  void should_delete_comment_throw_when_not_author() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", comment.getId()));
  }
}
