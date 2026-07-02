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
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "", "");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_create_article_success() {
    setAuthenticated(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    setAuthenticated(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();
    when(articleCommandService.createArticle(any(), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_when_create_article_not_authenticated() {
    setAnonymous();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("desc").body("body").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(any(), any())).thenReturn(article);
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("updated body")
            .description("updated desc")
            .build();

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_when_update_article_not_found() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("Updated").build();

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("not-exists", params));
  }

  @Test
  void should_throw_when_update_article_not_authorized() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    setAuthenticated(otherUser);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("Updated").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), params));
  }

  @Test
  void should_throw_when_update_article_not_authenticated() {
    setAnonymous();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("Updated").build();

    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), params));
  }

  @Test
  void should_favorite_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_favorite_article_not_found() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("not-exists"));
  }

  @Test
  void should_throw_when_favorite_article_not_authenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  void should_unfavorite_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(eq(fav));
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(any(), any())).thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_throw_when_unfavorite_article_not_authenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.unfavoriteArticle("slug"));
  }

  @Test
  void should_delete_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(eq(article));
  }

  @Test
  void should_throw_when_delete_article_not_authorized() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    setAuthenticated(otherUser);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
  }

  @Test
  void should_throw_when_delete_article_not_found() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("not-exists"));
  }

  @Test
  void should_throw_when_delete_article_not_authenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("slug"));
  }
}
