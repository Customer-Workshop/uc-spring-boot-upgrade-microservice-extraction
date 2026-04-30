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
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private CommentQueryService commentQueryService;

  private CommentMutation mutation;
  private User user;

  @BeforeEach
  void setUp() {
    mutation = new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_comment() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    CommentData commentData =
        new CommentData(
            "c1",
            "Great post",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result = mutation.createComment("title", "Great post");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_when_create_comment_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> mutation.createComment("slug", "body"));
  }

  @Test
  void should_throw_when_create_comment_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> mutation.createComment("missing", "body"));
  }

  @Test
  void should_delete_comment() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), "c1")).thenReturn(Optional.of(comment));

    DeletionStatus result = mutation.removeComment("title", "c1");

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_when_delete_comment_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> mutation.removeComment("slug", "c1"));
  }

  @Test
  void should_throw_when_delete_comment_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> mutation.removeComment("missing", "c1"));
  }

  @Test
  void should_throw_when_delete_comment_not_found() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "c1")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> mutation.removeComment("title", "c1"));
  }

  @Test
  void should_throw_when_delete_comment_no_authorization() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", List.of(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    when(commentRepository.findById(article.getId(), "c1")).thenReturn(Optional.of(comment));

    assertThrows(NoAuthorizationException.class, () -> mutation.removeComment("title", "c1"));
  }
}
