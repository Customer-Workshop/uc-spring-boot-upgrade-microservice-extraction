package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    setAnonymousAuth();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  public void should_create_article_success() {
    authenticateUser(user);
    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

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
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_create_article_with_null_taglist() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Title").description("desc").body("body").build();

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertNotNull(result);
  }

  @Test
  public void should_throw_auth_exception_when_creating_article_unauthenticated() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Title")
            .description("desc")
            .body("body")
            .build();
    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_success() {
    authenticateUser(user);
    Article article = new Article("Old Title", "desc", "body", Arrays.asList(), user.getId());
    Article updatedArticle =
        new Article("New Title", "new desc", "new body", Arrays.asList(), user.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(updatedArticle);

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("new body")
            .description("new desc")
            .build();

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), input);
    assertNotNull(result);
    assertEquals(updatedArticle, result.getLocalContext());
  }

  @Test
  public void should_throw_when_updating_article_not_found() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder().title("New Title").build();
    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("non-existent", input));
  }

  @Test
  public void should_throw_when_updating_article_not_author() {
    authenticateUser(user);
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList(), anotherUser.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    UpdateArticleInput input =
        UpdateArticleInput.newBuilder().title("New Title").build();
    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), input));
  }

  @Test
  public void should_favorite_article_success() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result =
        articleMutation.favoriteArticle(article.getSlug());
    assertNotNull(result);
    assertEquals(article, result.getLocalContext());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_when_favoriting_article_not_found() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("not-found"));
  }

  @Test
  public void should_throw_when_favoriting_unauthenticated() {
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  public void should_unfavorite_article_success() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result =
        articleMutation.unfavoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  public void should_unfavorite_article_when_no_favorite_exists() {
    authenticateUser(user);
    Article article = new Article("Title", "desc", "body", Arrays.asList(), user.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result =
        articleMutation.unfavoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
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
  public void should_throw_when_deleting_article_not_author() {
    authenticateUser(user);
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList(), anotherUser.getId());

    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.deleteArticle(article.getSlug()));
  }

  @Test
  public void should_throw_when_deleting_article_not_found() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("not-found"));
  }

  @Test
  public void should_throw_when_deleting_unauthenticated() {
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("slug"));
  }
}
