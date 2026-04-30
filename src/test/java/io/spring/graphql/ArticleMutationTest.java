package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("Description")
            .body("Body text")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_taglist() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("Description")
            .body("Body text")
            .build();

    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Title").description("Desc").body("Body").build();

    assertThrows(NullPointerException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("Updated Title").body("Updated Body").build();

    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(any(), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-title", changes);

    assertNotNull(result);
    assertEquals(article, result.getLocalContext());
  }

  @Test
  void should_update_article_throw_when_not_found() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", changes));
  }

  @Test
  void should_update_article_throw_when_not_authorized() {
    User otherUser = new User("other@example.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());

    when(articleRepository.findBySlug("other-article")).thenReturn(Optional.of(otherArticle));

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("Hijack").build();
    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("other-article", changes));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-title");

    assertNotNull(result);
    assertEquals(article, result.getLocalContext());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_favorite_article_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    assertThrows(NullPointerException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  void should_favorite_article_throw_when_not_found() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  void should_unfavorite_article_success() {
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  void should_unfavorite_article_no_existing_favorite() {
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));

    DeletionStatus status = articleMutation.deleteArticle("test-title");

    assertTrue(status.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_delete_article_throw_when_not_authorized() {
    User otherUser = new User("other@example.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug("other-article")).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("other-article"));
  }

  @Test
  void should_delete_article_throw_when_not_found() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  void should_delete_article_throw_when_not_authenticated() {
    SecurityContextHolder.clearContext();
    assertThrows(NullPointerException.class, () -> articleMutation.deleteArticle("slug"));
  }
}
