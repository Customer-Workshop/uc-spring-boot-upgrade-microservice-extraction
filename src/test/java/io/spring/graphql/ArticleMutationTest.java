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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class ArticleMutationTest {

  private ArticleMutation articleMutation;
  private ArticleCommandService articleCommandService;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;
  private User user;

  @BeforeEach
  void setUp() {
    articleCommandService = mock(ArticleCommandService.class);
    articleFavoriteRepository = mock(ArticleFavoriteRepository.class);
    articleRepository = mock(ArticleRepository.class);
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_article_successfully() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article =
        new Article("Test Title", "Test Description", "Test Body", Arrays.asList("java", "spring"), user.getId());
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
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
        new Article("Test Title", "Test Description", "Test Body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  void should_throw_when_creating_article_without_auth() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test")
            .description("Desc")
            .body("Body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_successfully() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("old-title", input);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
  }

  @Test
  void should_throw_when_update_article_not_found() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", input));
  }

  @Test
  void should_throw_when_update_article_no_authorization() {
    User otherUser = new User("other@example.com", "other", "password", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("title", input));
  }

  @Test
  void should_favorite_article() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_unfavorite_article() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_when_no_existing_favorite() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article() {
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_when_delete_article_no_authorization() {
    User otherUser = new User("other@example.com", "other", "password", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
