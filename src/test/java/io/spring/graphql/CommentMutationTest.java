package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.TestingAuthenticationToken;
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
            "comment-id",
            "Nice article",
            article.getId(),
            new DateTime(),
            new DateTime(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment(article.getSlug(), "Nice article");

    assertNotNull(result);
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  void should_throw_when_unauthenticated_create_comment() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(
        AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
  }

  @Test
  void should_throw_not_found_when_create_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment("nonexistent", "body"));
  }

  @Test
  void should_delete_comment_success() {
    Comment comment = new Comment("Nice", user.getId(), article.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), comment.getId()))
        .thenReturn(Optional.of(comment));

    DeletionStatus result = commentMutation.removeComment(article.getSlug(), comment.getId());

    assertTrue(result.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  void should_throw_when_unauthenticated_delete_comment() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(
        AuthenticationException.class, () -> commentMutation.removeComment("slug", "comment-id"));
  }

  @Test
  void should_throw_not_found_when_delete_comment_on_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment("nonexistent", "comment-id"));
  }

  @Test
  void should_throw_not_found_when_comment_not_found() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(article.getId(), "missing")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment(article.getSlug(), "missing"));
  }

  @Test
  void should_throw_no_authorization_when_delete_other_user_comment() {
    User other = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList("java"), other.getId());
    Comment otherComment = new Comment("Nice", other.getId(), otherArticle.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));
    when(commentRepository.findById(otherArticle.getId(), otherComment.getId()))
        .thenReturn(Optional.of(otherComment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment(otherArticle.getSlug(), otherComment.getId()));
  }
}
