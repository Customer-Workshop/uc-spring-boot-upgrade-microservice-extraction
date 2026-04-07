package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
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
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuthentication() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
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
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertNotNull(result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
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
        new Article("Test Title", "Test Description", "Test Body", new ArrayList<>(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_when_create_article_unauthenticated() {
    setAnonymousAuthentication();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", new ArrayList<>(), user.getId());
    when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(article));

    Article updatedArticle =
        new Article("New Title", "New Desc", "New Body", new ArrayList<>(), user.getId());
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updatedArticle);

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle("old-title", changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_when_update_article_not_found() {
    when(articleRepository.findBySlug("not-found")).thenReturn(Optional.empty());

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("not-found", changes));
  }

  @Test
  void should_throw_when_update_article_unauthenticated() {
    setAnonymousAuthentication();
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), "other-user-id");
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        AuthenticationException.class, () -> articleMutation.updateArticle("title", changes));
  }

  @Test
  void should_throw_when_update_article_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), anotherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("title", changes));
  }

  @Test
  void should_favorite_article_success() {
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_favorite_article_unauthenticated() {
    setAnonymousAuthentication();
    assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle("title"));
  }

  @Test
  void should_throw_when_favorite_article_not_found() {
    when(articleRepository.findBySlug("not-found")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("not-found"));
  }

  @Test
  void should_unfavorite_article_success() {
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(favorite));
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
  }

  @Test
  void should_throw_when_unfavorite_article_unauthenticated() {
    setAnonymousAuthentication();
    assertThrows(
        AuthenticationException.class, () -> articleMutation.unfavoriteArticle("title"));
  }

  @Test
  void should_throw_when_unfavorite_article_not_found() {
    when(articleRepository.findBySlug("not-found")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.unfavoriteArticle("not-found"));
  }

  @Test
  void should_delete_article_success() {
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  void should_throw_when_delete_article_unauthenticated() {
    setAnonymousAuthentication();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("title"));
  }

  @Test
  void should_throw_when_delete_article_not_found() {
    when(articleRepository.findBySlug("not-found")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("not-found"));
  }

  @Test
  void should_throw_when_delete_article_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", new ArrayList<>(), anotherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
