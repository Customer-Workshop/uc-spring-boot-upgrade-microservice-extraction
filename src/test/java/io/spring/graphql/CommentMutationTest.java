package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    setAnonymousAuth();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  public void should_create_comment_success() {
    authenticateUser(user);
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "comment-id",
            "comment body",
            article.getId(),
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment(article.getSlug(), "comment body");
    assertNotNull(result);
    assertEquals(commentData, result.getLocalContext());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_throw_when_creating_comment_unauthenticated() {
    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  public void should_throw_when_creating_comment_article_not_found() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("not-found", "body"));
  }

  @Test
  public void should_delete_comment_success() {
    authenticateUser(user);
    Comment comment = new Comment("body", user.getId(), article.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    DeletionStatus result =
        commentMutation.removeComment(article.getSlug(), comment.getId());
    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  public void should_throw_when_deleting_comment_unauthenticated() {
    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment("slug", "comment-id"));
  }

  @Test
  public void should_throw_when_deleting_comment_article_not_found() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("not-found", "comment-id"));
  }

  @Test
  public void should_throw_when_deleting_comment_not_found() {
    authenticateUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("bad-id")))
        .thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment(article.getSlug(), "bad-id"));
  }

  @Test
  public void should_throw_when_deleting_comment_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    authenticateUser(anotherUser);

    User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
    Article ownerArticle =
        new Article("Title", "desc", "body", Arrays.asList(), articleOwner.getId());
    Comment comment = new Comment("body", user.getId(), ownerArticle.getId());

    when(articleRepository.findBySlug(eq(ownerArticle.getSlug())))
        .thenReturn(Optional.of(ownerArticle));
    when(commentRepository.findById(eq(ownerArticle.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment(ownerArticle.getSlug(), comment.getId()));
  }
}
