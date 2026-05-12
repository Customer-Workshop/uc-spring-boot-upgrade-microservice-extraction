package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  @Test
  void should_create_article_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
          .thenReturn(article);

      CreateArticleInput input =
          CreateArticleInput.newBuilder()
              .title("Test Title")
              .description("desc")
              .body("body")
              .tagList(Arrays.asList("java"))
              .build();

      DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

      assertNotNull(result);
      assertNotNull(result.getData());
      assertEquals(article, result.getLocalContext());
      verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
    }
  }

  @Test
  void should_throw_authentication_exception_when_creating_article_without_login() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      CreateArticleInput input =
          CreateArticleInput.newBuilder()
              .title("Test Title")
              .description("desc")
              .body("body")
              .build();

      assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
    }
  }

  @Test
  void should_create_article_with_empty_tag_list() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
          .thenReturn(article);

      CreateArticleInput input =
          CreateArticleInput.newBuilder().title("Test").description("desc").body("body").build();

      DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
      assertNotNull(result);
    }
  }

  @Test
  void should_update_article_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
          .thenReturn(article);

      UpdateArticleInput params =
          UpdateArticleInput.newBuilder().title("New Title").body("New Body").build();

      DataFetcherResult<ArticlePayload> result =
          articleMutation.updateArticle(article.getSlug(), params);

      assertNotNull(result);
      verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
    }
  }

  @Test
  void should_throw_not_found_when_updating_nonexistent_article() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

      UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New Title").build();

      assertThrows(
          ResourceNotFoundException.class,
          () -> articleMutation.updateArticle("nonexistent", params));
    }
  }

  @Test
  void should_throw_no_authorization_when_non_author_updates() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      User anotherUser = new User("other@test.com", "other", "pass", "", "");
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(anotherUser));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

      assertThrows(
          NoAuthorizationException.class,
          () -> articleMutation.updateArticle(article.getSlug(), params));
    }
  }

  @Test
  void should_favorite_article_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

      assertNotNull(result);
      verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
    }
  }

  @Test
  void should_throw_not_found_when_favoriting_nonexistent_article() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
    }
  }

  @Test
  void should_unfavorite_article_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
      when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
          .thenReturn(Optional.of(fav));

      DataFetcherResult<ArticlePayload> result =
          articleMutation.unfavoriteArticle(article.getSlug());

      assertNotNull(result);
      verify(articleFavoriteRepository).remove(eq(fav));
    }
  }

  @Test
  void should_unfavorite_article_when_not_favorited() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
      when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
          .thenReturn(Optional.empty());

      DataFetcherResult<ArticlePayload> result =
          articleMutation.unfavoriteArticle(article.getSlug());

      assertNotNull(result);
      verify(articleFavoriteRepository, never()).remove(any());
    }
  }

  @Test
  void should_delete_article_success() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

      assertTrue(result.getSuccess());
      verify(articleRepository).remove(article);
    }
  }

  @Test
  void should_throw_no_authorization_when_non_author_deletes() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      User anotherUser = new User("other@test.com", "other", "pass", "", "");
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(anotherUser));
      when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

      assertThrows(
          NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
    }
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_article() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

      assertThrows(
          ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
    }
  }

  @Test
  void should_throw_authentication_when_unauthenticated_user_favorites() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(
          AuthenticationException.class, () -> articleMutation.favoriteArticle("some-slug"));
    }
  }

  @Test
  void should_throw_authentication_when_unauthenticated_user_deletes() {
    try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
      securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());

      assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("some-slug"));
    }
  }
}
