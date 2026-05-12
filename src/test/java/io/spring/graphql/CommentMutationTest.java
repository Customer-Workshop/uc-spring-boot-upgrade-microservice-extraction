package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import io.spring.graphql.types.DeletionStatus;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation commentMutation;
  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "description", "body", Arrays.asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(u, null));
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
  }

  @Test
  void should_create_comment_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "comment-id",
            "Great article!",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

    var result = commentMutation.createComment("test-title", "Great article!");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_authentication_when_creating_comment_unauthenticated() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("test-title", "body"));
  }

  @Test
  void should_throw_not_found_when_article_doesnt_exist_for_comment() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("non-existent", "body"));
  }

  @Test
  void should_throw_not_found_when_saved_comment_not_found() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("test-title", "body"));
  }

  @Test
  void should_delete_comment_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(eq(article.getId()), anyString()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-title", comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_authentication_when_deleting_comment_unauthenticated() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("test-title", "comment-id"));
  }

  @Test
  void should_throw_not_found_when_article_doesnt_exist_for_delete_comment() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("non-existent", "comment-id"));
  }

  @Test
  void should_throw_not_found_when_comment_doesnt_exist() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-title", "non-existent"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_other_users_comment() {
    setAuthenticated(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article otherArticle =
        new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment otherComment = new Comment("body", otherUser.getId(), otherArticle.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(eq(otherArticle.getId()), anyString()))
        .thenReturn(Optional.of(otherComment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", otherComment.getId()));
  }
}
