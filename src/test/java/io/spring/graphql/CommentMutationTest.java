package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

  @InjectMocks private CommentMutation commentMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
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
            "commentId",
            "Great article!",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    var result = commentMutation.createComment("test-title", "Great article!");

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_authentication_when_creating_comment_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment("test-title", "comment"));
  }

  @Test
  void should_throw_not_found_when_creating_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "comment"));
  }

  @Test
  void should_delete_comment_success() {
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("commentId")))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("test-title", "commentId");

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_authentication_when_deleting_comment_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("test-title", "commentId"));
  }

  @Test
  void should_throw_not_found_when_deleting_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "commentId"));
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_comment() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("commentId")))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-title", "commentId"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_others_comment() {
    User otherUser = new User("other@test.com", "otheruser", "pass", "", "");
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment otherComment = new Comment("body", otherUser.getId(), otherArticle.getId());

    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(eq(otherArticle.getId()), eq("commentId")))
        .thenReturn(Optional.of(otherComment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("other-title", "commentId"));
  }
}
