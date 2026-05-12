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
    user = new User("test@test.com", "testuser", "password", "", "");
    article =
        new Article(
            "Test Title", "desc", "body", Arrays.asList("java"), user.getId(), new DateTime());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment_success() {
    String slug = "test-title";
    String body = "Nice article!";
    CommentData commentData =
        new CommentData(
            "comment-id",
            body,
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = commentMutation.createComment(slug, body);

    assertNotNull(result);
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_not_found_when_create_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void should_throw_authentication_when_create_comment_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_ANONYMOUS"))));
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_delete_comment_success() {
    String slug = "test-title";
    String commentId = "comment-id";
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(articleRepository.findBySlug(slug)).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), commentId)).thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment(slug, commentId);

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_not_found_when_delete_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("nonexistent", "id"));
  }

  @Test
  void should_throw_not_found_when_delete_nonexistent_comment() {
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "no-id")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("test-title", "no-id"));
  }

  @Test
  void should_throw_no_authorization_when_delete_others_comment() {
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article othersArticle =
        new Article(
            "Other Article", "desc", "body", Arrays.asList(), articleOwner.getId(), new DateTime());
    User commentOwner = new User("commenter@test.com", "commenter", "pass", "", "");
    Comment otherComment = new Comment("body", commentOwner.getId(), othersArticle.getId());
    when(articleRepository.findBySlug("other-article")).thenReturn(Optional.of(othersArticle));
    when(commentRepository.findById(othersArticle.getId(), "cid"))
        .thenReturn(Optional.of(otherComment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("other-article", "cid"));
  }
}
