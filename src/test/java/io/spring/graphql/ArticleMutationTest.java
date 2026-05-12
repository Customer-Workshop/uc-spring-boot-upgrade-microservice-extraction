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
  public void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  public void should_create_article_successfully() {
    authenticateUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();
    Article article =
        new Article(
            "Test Title", "Test Desc", "Test Body", Arrays.asList("java", "spring"), user.getId());
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  public void should_create_article_with_null_tag_list() {
    authenticateUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .build();
    Article article =
        new Article("Test Title", "Test Desc", "Test Body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  public void should_throw_authentication_exception_when_creating_article_unauthenticated() {
    setAnonymous();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("Desc").body("Body").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_successfully() {
    authenticateUser(user);
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();
    when(articleRepository.findBySlug(eq("old-title"))).thenReturn(Optional.of(article));
    Article updatedArticle =
        new Article("New Title", "New Desc", "New Body", Collections.emptyList(), user.getId());
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updatedArticle);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("old-title", params);

    assertNotNull(result);
    assertEquals(updatedArticle, result.getLocalContext());
  }

  @Test
  public void should_throw_not_found_when_updating_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", params));
  }

  @Test
  public void should_throw_no_authorization_when_non_author_updates_article() {
    authenticateUser(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("title", params));
  }

  @Test
  public void should_favorite_article_successfully() {
    authenticateUser(user);
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), "other-user-id");
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_authentication_exception_when_favoriting_unauthenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  public void should_throw_not_found_when_favoriting_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  public void should_unfavorite_article_successfully() {
    authenticateUser(user);
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), "other-user-id");
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(fav));
  }

  @Test
  public void should_unfavorite_article_when_no_favorite_exists() {
    authenticateUser(user);
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), "other-user-id");
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  public void should_delete_article_successfully() {
    authenticateUser(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  public void should_throw_authentication_exception_when_deleting_unauthenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("slug"));
  }

  @Test
  public void should_throw_not_found_when_deleting_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  public void should_throw_no_authorization_when_non_author_deletes_article() {
    authenticateUser(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug(eq("title"))).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
