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
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.CommentPayload;
import io.spring.graphql.types.DeletionStatus;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class CommentMutationTest {

  private CommentMutation commentMutation;
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  void setUp() {
    articleRepository = mock(ArticleRepository.class);
    commentRepository = mock(CommentRepository.class);
    commentQueryService = mock(CommentQueryService.class);
    commentMutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_successfully() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    ProfileData profileData = new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData =
        new CommentData("commentId", "Test comment body", article.getId(), new DateTime(), new DateTime(), profileData);
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "Test comment body");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
    verify(commentQueryService).findById(any(), eq(user));
  }

  @Test
  void should_throw_when_creating_comment_without_auth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment("title", "body"));
  }

  @Test
  void should_throw_when_creating_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void should_delete_comment_successfully() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("Test body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), "comment-1"))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", "comment-1");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_when_deleting_nonexistent_comment() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "nonexistent"))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("title", "nonexistent"));
  }

  @Test
  void should_throw_when_deleting_comment_without_authorization() {
    User otherUser = new User("other@example.com", "other", "password", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("Test body", otherUser.getId(), article.getId());
    when(commentRepository.findById(article.getId(), "comment-1"))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", "comment-1"));
  }
}
