package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.junit.jupiter.api.AfterEach;
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
  private Article article;
  private MockedStatic<SecurityUtil> securityUtilMock;

  @BeforeEach
  void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    article = new Article("Test Title", "Desc", "Body", Arrays.asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    if (securityUtilMock != null) {
      securityUtilMock.close();
    }
  }

  private void mockAuthenticated() {
    securityUtilMock = mockStatic(SecurityUtil.class);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
  }

  private void mockUnauthenticated() {
    securityUtilMock = mockStatic(SecurityUtil.class);
    securityUtilMock.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
  }

  @Test
  void should_create_article_successfully() {
    mockAuthenticated();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList("java"))
            .build();

    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    mockAuthenticated();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Desc")
            .body("Body")
            .build();

    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  void should_throw_when_create_article_unauthenticated() {
    mockUnauthenticated();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("Desc").body("Body").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_successfully() {
    mockAuthenticated();
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("Updated Body")
            .description("Updated Desc")
            .build();

    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-slug", input);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
  }

  @Test
  void should_throw_when_update_article_not_found() {
    mockAuthenticated();
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New").build();
    when(articleRepository.findBySlug("unknown")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("unknown", input));
  }

  @Test
  void should_throw_when_update_article_unauthenticated() {
    mockUnauthenticated();
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New").build();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    assertThrows(
        AuthenticationException.class, () -> articleMutation.updateArticle("test-slug", input));
  }

  @Test
  void should_throw_when_update_article_not_authorized() {
    User otherUser = new User("other@test.com", "other", "123", "", "");
    article = new Article("Title", "Desc", "Body", Arrays.asList("java"), otherUser.getId());

    mockAuthenticated();
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New").build();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("test-slug", input));
  }

  @Test
  void should_favorite_article_successfully() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-slug");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_favorite_article_unauthenticated() {
    mockUnauthenticated();
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("test-slug"));
  }

  @Test
  void should_throw_when_favorite_article_not_found() {
    mockAuthenticated();
    when(articleRepository.findBySlug("unknown")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("unknown"));
  }

  @Test
  void should_unfavorite_article_successfully() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-slug");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_throw_when_unfavorite_article_unauthenticated() {
    mockUnauthenticated();
    assertThrows(
        AuthenticationException.class, () -> articleMutation.unfavoriteArticle("test-slug"));
  }

  @Test
  void should_delete_article_successfully() {
    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-slug");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_when_delete_article_unauthenticated() {
    mockUnauthenticated();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("test-slug"));
  }

  @Test
  void should_throw_when_delete_article_not_found() {
    mockAuthenticated();
    when(articleRepository.findBySlug("unknown")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("unknown"));
  }

  @Test
  void should_throw_when_delete_article_not_authorized() {
    User otherUser = new User("other@test.com", "other", "123", "", "");
    article = new Article("Title", "Desc", "Body", Arrays.asList("java"), otherUser.getId());

    mockAuthenticated();
    when(articleRepository.findBySlug("test-slug")).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("test-slug"));
  }
}
