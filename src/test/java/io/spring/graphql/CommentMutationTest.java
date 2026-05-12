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

  private CommentMutation commentMutation;
  private User user;

  @BeforeEach
  void setUp() {
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void should_create_comment_when_authenticated() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    CommentData commentData =
        new CommentData(
            "comment-id",
            "Great article!",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "Great article!");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_authentication_when_creating_comment_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_throw_not_found_when_creating_comment_for_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("missing", "body"));
  }

  @Test
  void should_throw_not_found_when_comment_data_not_found_after_save() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("title", "body"));
  }

  @Test
  void should_delete_comment_when_authorized() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_not_found_when_deleting_comment_from_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("missing", "comment-id"));
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_comment() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "missing-comment"))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("title", "missing-comment"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_others_comment() {
    setAuthenticated(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), articleOwner.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    Comment comment = new Comment("body", otherUser.getId(), article.getId());
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment("title", comment.getId()));
  }
}
