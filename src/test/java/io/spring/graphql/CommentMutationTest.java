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
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
  }

  @Test
  void should_create_comment_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      CommentData commentData =
          new CommentData(
              "comment-id",
              "body",
              article.getId(),
              new DateTime(),
              new DateTime(),
              new ProfileData(user.getId(), user.getUsername(), "", "", false));
      when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.of(commentData));

      DataFetcherResult<CommentPayload> result =
          commentMutation.createComment(article.getSlug(), "body");

      assertNotNull(result);
      assertEquals(commentData, result.getLocalContext());
      verify(commentRepository).save(any(Comment.class));
    }
  }

  @Test
  void should_throw_authentication_when_creating_comment_without_login() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> commentMutation.createComment("slug", "body"));
    }
  }

  @Test
  void should_throw_not_found_when_creating_comment_on_nonexistent_article() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> commentMutation.createComment("nonexistent", "body"));
    }
  }

  @Test
  void should_throw_not_found_when_comment_data_not_found_after_save() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(commentQueryService.findById(any(), eq(user))).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> commentMutation.createComment(article.getSlug(), "body"));
    }
  }

  @Test
  void should_delete_comment_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      Comment comment = new Comment("body", user.getId(), article.getId());
      when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
          .thenReturn(Optional.of(comment));

      DeletionStatus result = commentMutation.removeComment(article.getSlug(), comment.getId());

      assertTrue(result.getSuccess());
      verify(commentRepository).remove(comment);
    }
  }

  @Test
  void should_throw_authentication_when_deleting_comment_without_login() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> commentMutation.removeComment("slug", "comment-id"));
    }
  }

  @Test
  void should_throw_not_found_when_article_not_found_for_delete_comment() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> commentMutation.removeComment("nonexistent", "comment-id"));
    }
  }

  @Test
  void should_throw_not_found_when_comment_not_found() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(commentRepository.findById(eq(article.getId()), eq("nonexistent")))
          .thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> commentMutation.removeComment(article.getSlug(), "nonexistent"));
    }
  }

  @Test
  void should_throw_no_authorization_when_non_author_deletes_comment() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      User anotherUser = new User("other@test.com", "other", "pass", "", "");
      User articleOwner = new User("owner@test.com", "owner", "pass", "", "");
      Article ownedArticle =
          new Article("Title", "desc", "body", Arrays.asList("tag"), articleOwner.getId());

      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(anotherUser));
      when(articleRepository.findBySlug(eq(ownedArticle.getSlug())))
          .thenReturn(Optional.of(ownedArticle));

      Comment comment = new Comment("body", articleOwner.getId(), ownedArticle.getId());
      when(commentRepository.findById(eq(ownedArticle.getId()), eq(comment.getId())))
          .thenReturn(Optional.of(comment));

      assertThrows(
          NoAuthorizationException.class,
          () -> commentMutation.removeComment(ownedArticle.getSlug(), comment.getId()));
    }
  }
}
