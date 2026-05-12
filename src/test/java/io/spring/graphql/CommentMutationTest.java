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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
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
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  void should_create_comment_successfully() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "commentId",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment(article.getSlug(), "comment body");

    assertNotNull(result);
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_auth_exception_when_creating_comment_unauthenticated() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("some-slug", "body"));
  }

  @Test
  void should_throw_not_found_when_creating_comment_for_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void should_throw_not_found_when_comment_data_not_found_after_save() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment(article.getSlug(), "body"));
  }

  @Test
  void should_delete_comment_successfully() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(eq(article.getId()), anyString()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment(article.getSlug(), comment.getId());
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_auth_exception_when_deleting_comment_unauthenticated() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment("slug", "commentId"));
  }

  @Test
  void should_throw_not_found_when_deleting_comment_for_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "commentId"));
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_comment() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment(article.getSlug(), "nonexistent"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_other_users_comment() {
    authenticateUser(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle = new Article("Title", "Desc", "Body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));
    Comment comment = new Comment("body", otherUser.getId(), otherArticle.getId());
    when(commentRepository.findById(eq(otherArticle.getId()), anyString()))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment(otherArticle.getSlug(), comment.getId()));
  }
}
