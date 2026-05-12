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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
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
  public void setUp() {
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_create_comment_successfully() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment("title", "comment body");

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_authentication_exception_when_creating_comment_unauthenticated() {
    setAnonymous();

    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("title", "body"));
  }

  @Test
  public void should_throw_not_found_when_creating_comment_on_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  public void should_delete_comment_successfully() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    Comment comment = new Comment("body", user.getId(), article.getId());
    when(commentRepository.findById(eq(article.getId()), eq("comment-id")))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment("title", "comment-id");

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(eq(comment));
  }

  @Test
  public void should_throw_authentication_exception_when_deleting_comment_unauthenticated() {
    setAnonymous();

    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment("title", "comment-id"));
  }

  @Test
  public void should_throw_not_found_when_deleting_comment_on_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "comment-id"));
  }

  @Test
  public void should_throw_not_found_when_deleting_nonexistent_comment() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("missing")))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("title", "missing"));
  }

  @Test
  public void should_throw_no_authorization_when_non_author_deletes_comment() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    authenticateUser(otherUser);
    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article ownerArticle =
        new Article("Title", "Desc", "Body", Collections.emptyList(), articleOwner.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(ownerArticle));
    Comment comment = new Comment("body", user.getId(), ownerArticle.getId());
    when(commentRepository.findById(eq(ownerArticle.getId()), eq("comment-id")))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class, () -> commentMutation.removeComment("title", "comment-id"));
  }
}
