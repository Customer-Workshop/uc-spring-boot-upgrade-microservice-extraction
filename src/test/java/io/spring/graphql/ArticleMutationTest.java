package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import io.spring.graphql.types.CreateArticleInput;
import io.spring.graphql.types.DeletionStatus;
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;

  @BeforeEach
  void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article =
        new Article("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(article, result.getLocalContext());
      verify(articleCommandService).createArticle(any(), eq(user));
    }
  }

  @Test
  void should_create_article_with_null_tag_list() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .build();

    Article article =
        new Article("Test Title", "Test Description", "Test Body", Arrays.asList(), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

      DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

      assertNotNull(result);
      verify(articleCommandService).createArticle(any(), eq(user));
    }
  }

  @Test
  void should_throw_authentication_exception_when_creating_without_user() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .build();

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
    }
  }

  @Test
  void should_update_article_success() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList("java"), user.getId());
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();

    Article updatedArticle =
        new Article("New Title", "New Desc", "New Body", Arrays.asList("java"), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updatedArticle);

      DataFetcherResult<ArticlePayload> result =
          articleMutation.updateArticle(article.getSlug(), params);

      assertNotNull(result);
      assertEquals(updatedArticle, result.getLocalContext());
    }
  }

  @Test
  void should_throw_when_updating_nonexistent_article() {
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").build();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", params));
  }

  @Test
  void should_throw_no_authorization_when_updating_others_article() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), anotherUser.getId());

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").build();

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      assertThrows(
          NoAuthorizationException.class,
          () -> articleMutation.updateArticle(article.getSlug(), params));
    }
  }

  @Test
  void should_throw_authentication_when_updating_without_user() {
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").build();

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      assertThrows(
          AuthenticationException.class,
          () -> articleMutation.updateArticle(article.getSlug(), params));
    }
  }

  @Test
  void should_favorite_article_success() {
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      DataFetcherResult<ArticlePayload> result =
          articleMutation.favoriteArticle(article.getSlug());

      assertNotNull(result);
      assertEquals(article, result.getLocalContext());
      verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
    }
  }

  @Test
  void should_throw_authentication_when_favoriting_without_user() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> articleMutation.favoriteArticle("some-slug"));
    }
  }

  @Test
  void should_throw_when_favoriting_nonexistent_article() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleMutation.favoriteArticle("nonexistent"));
    }
  }

  @Test
  void should_unfavorite_article_success() {
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleFavoriteRepository.find(article.getId(), user.getId()))
          .thenReturn(Optional.of(favorite));

      DataFetcherResult<ArticlePayload> result =
          articleMutation.unfavoriteArticle(article.getSlug());

      assertNotNull(result);
      assertEquals(article, result.getLocalContext());
      verify(articleFavoriteRepository).remove(favorite);
    }
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleFavoriteRepository.find(article.getId(), user.getId()))
          .thenReturn(Optional.empty());

      DataFetcherResult<ArticlePayload> result =
          articleMutation.unfavoriteArticle(article.getSlug());

      assertNotNull(result);
      verify(articleFavoriteRepository, never()).remove(any());
    }
  }

  @Test
  void should_throw_authentication_when_unfavoriting_without_user() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> articleMutation.unfavoriteArticle("some-slug"));
    }
  }

  @Test
  void should_delete_article_success() {
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), user.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

      assertNotNull(result);
      assertTrue(result.getSuccess());
      verify(articleRepository).remove(article);
    }
  }

  @Test
  void should_throw_no_authorization_when_deleting_others_article() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Arrays.asList("java"), anotherUser.getId());

    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      assertThrows(
          NoAuthorizationException.class,
          () -> articleMutation.deleteArticle(article.getSlug()));
    }
  }

  @Test
  void should_throw_authentication_when_deleting_without_user() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> articleMutation.deleteArticle("some-slug"));
    }
  }

  @Test
  void should_throw_when_deleting_nonexistent_article() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleMutation.deleteArticle("nonexistent"));
    }
  }
}
