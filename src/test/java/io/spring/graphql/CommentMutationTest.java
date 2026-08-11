package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentMutationTest extends GraphQLTestBase {

  @Mock private ArticleRepository articleRepository;

  @Mock private CommentRepository commentRepository;

  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentMutation commentMutation;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  private CommentData commentData(String id) {
    return new CommentData(
        id,
        "content",
        article.getId(),
        new DateTime(),
        new DateTime(),
        new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));
  }

  @Test
  public void should_create_comment() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user)))
        .thenReturn(Optional.of(commentData("comment-id")));

    DataFetcherResult<CommentPayload> result =
        commentMutation.createComment(article.getSlug(), "content");

    assertEquals("comment-id", ((CommentData) result.getLocalContext()).getId());
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_not_create_comment_for_anonymous_user() {
    anonymous();

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.createComment(article.getSlug(), "content"));
    verify(commentRepository, never()).save(any());
  }

  @Test
  public void should_not_create_comment_on_unknown_article() {
    authenticate(user);
    when(articleRepository.findBySlug(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.createComment("ghost", "content"));
  }

  @Test
  public void should_throw_not_found_when_created_comment_cannot_be_read() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.createComment(article.getSlug(), "content"));
  }

  @Test
  public void should_remove_own_comment() {
    authenticate(user);
    Comment comment = new Comment("content", user.getId(), article.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    DeletionStatus status = commentMutation.removeComment(article.getSlug(), comment.getId());

    assertTrue(status.getSuccess());
    verify(commentRepository).remove(comment);
  }

  @Test
  public void should_not_remove_comment_of_other_user_on_other_article() {
    User other = new User("other@test.com", "other", "123", "", "");
    authenticate(other);
    Comment comment = new Comment("content", user.getId(), article.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    assertThrows(
        NoAuthorizationException.class,
        () -> commentMutation.removeComment(article.getSlug(), comment.getId()));
    verify(commentRepository, never()).remove(any());
  }

  @Test
  public void should_not_remove_unknown_comment() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(commentRepository.findById(eq(article.getId()), eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> commentMutation.removeComment(article.getSlug(), "ghost"));
  }

  @Test
  public void should_not_remove_comment_of_unknown_article() {
    authenticate(user);
    when(articleRepository.findBySlug(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> commentMutation.removeComment("ghost", "comment"));
  }

  @Test
  public void should_not_remove_comment_for_anonymous_user() {
    anonymous();

    assertThrows(
        AuthenticationException.class,
        () -> commentMutation.removeComment(article.getSlug(), "comment"));
  }
}
