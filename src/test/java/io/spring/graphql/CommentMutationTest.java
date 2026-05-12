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
    user = new User("test@test.com", "testuser", "123", "", "");
    article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    commentMutation =
        new CommentMutation(articleRepository, commentRepository, commentQueryService);
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
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_create_comment_success() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    CommentData commentData =
        new CommentData(
            "commentId",
            "comment body",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment(article.getSlug(), "comment body");
    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_auth_when_not_logged_in_create_comment() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  public void should_throw_not_found_when_article_not_exists_create_comment() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  public void should_delete_comment_success() {
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
  public void should_throw_auth_when_not_logged_in_delete_comment() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment("slug", "commentId"));
  }

  @Test
  public void should_throw_not_found_when_article_not_exists_delete_comment() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "commentId"));
  }

  @Test
  public void should_throw_not_found_when_comment_not_exists_delete_comment() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment(article.getSlug(), "nonexistent"));
  }

  @Test
  public void should_throw_no_authorization_when_not_author_delete_comment() {
    authenticateUser(user);
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    User articleOwner = new User("owner@test.com", "owner", "123", "", "");
    Article otherArticle =
        new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", anotherUser.getId(), otherArticle.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(eq(otherArticle.getId()), anyString()))
        .thenReturn(Optional.of(comment));
    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment(otherArticle.getSlug(), comment.getId()));
  }
}
