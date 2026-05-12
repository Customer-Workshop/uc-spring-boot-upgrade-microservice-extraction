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
    user = new User("test@test.com", "testuser", "123", "", "");
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
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
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_create_article_success() {
    authenticateUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_create_article_with_null_taglist() {
    authenticateUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();
    Article article = new Article("Test Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertNotNull(result);
  }

  @Test
  public void should_throw_authentication_exception_when_not_logged_in_create() {
    setAnonymous();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();
    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_success() {
    authenticateUser(user);
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    Article updated =
        new Article("New Title", "new desc", "new body", Arrays.asList("java"), user.getId());
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updated);

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("new desc")
            .body("new body")
            .build();
    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), changes);
    assertNotNull(result);
  }

  @Test
  public void should_throw_not_found_when_update_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();
    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", changes));
  }

  @Test
  public void should_throw_no_authorization_when_not_author_to_update() {
    authenticateUser(user);
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), anotherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();
    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), changes));
  }

  @Test
  public void should_throw_authentication_when_not_logged_in_update() {
    setAnonymous();
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("x").build();
    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), changes));
  }

  @Test
  public void should_favorite_article_success() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_not_found_when_favorite_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  public void should_throw_auth_when_not_logged_in_favorite() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  public void should_unfavorite_article_success() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_unfavorite_article_when_no_existing_favorite() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  public void should_throw_auth_when_not_logged_in_unfavorite() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.unfavoriteArticle("slug"));
  }

  @Test
  public void should_delete_article_success() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  public void should_throw_not_found_when_delete_nonexistent() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  public void should_throw_no_authorization_when_not_author_to_delete() {
    authenticateUser(user);
    User anotherUser = new User("other@test.com", "other", "123", "", "");
    Article article = new Article("Title", "desc", "body", Arrays.asList(), anotherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
  }

  @Test
  public void should_throw_auth_when_not_logged_in_delete() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("slug"));
  }
}
