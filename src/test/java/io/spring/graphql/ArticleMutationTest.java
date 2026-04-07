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
  private Article article;

  @BeforeEach
  void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article =
        new Article("Test Title", "description", "body", Arrays.asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthentication(User user) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymousAuthentication() {
    AnonymousAuthenticationToken anonymousAuth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
  }

  @Test
  void should_create_article_successfully() {
    setAuthentication(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    setAuthentication(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_when_create_article_not_authenticated() {
    setAnonymousAuthentication();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_successfully() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Description")
            .build();

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_when_update_article_not_found() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("non-existent", params));
  }

  @Test
  void should_throw_when_update_article_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "password", "", "");
    setAuthentication(user);
    Article otherArticle =
        new Article(
            "Other Title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(otherArticle.getSlug(), params));
  }

  @Test
  void should_throw_when_update_article_not_authenticated() {
    setAnonymousAuthentication();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), params));
  }

  @Test
  void should_favorite_article_successfully() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result =
        articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_favorite_article_not_authenticated() {
    setAnonymousAuthentication();
    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.favoriteArticle(article.getSlug()));
  }

  @Test
  void should_throw_when_favorite_article_not_found() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.favoriteArticle("non-existent"));
  }

  @Test
  void should_unfavorite_article_successfully() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    ArticleFavorite articleFavorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(articleFavorite));

    DataFetcherResult<ArticlePayload> result =
        articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(articleFavorite));
  }

  @Test
  void should_unfavorite_article_when_favorite_not_found() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result =
        articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_throw_when_unfavorite_article_not_authenticated() {
    setAnonymousAuthentication();
    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.unfavoriteArticle(article.getSlug()));
  }

  @Test
  void should_delete_article_successfully() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  void should_throw_when_delete_article_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "password", "", "");
    setAuthentication(user);
    Article otherArticle =
        new Article(
            "Other Title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.deleteArticle(otherArticle.getSlug()));
  }

  @Test
  void should_throw_when_delete_article_not_found() {
    setAuthentication(user);
    when(articleRepository.findBySlug(eq("non-existent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.deleteArticle("non-existent"));
  }

  @Test
  void should_throw_when_delete_article_not_authenticated() {
    setAnonymousAuthentication();
    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.deleteArticle(article.getSlug()));
  }
}
