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
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                u, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))));
  }

  @Test
  void should_create_article_when_authenticated() {
    setAuthenticated(user);
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
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    setAuthenticated(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .build();

    Article article =
        new Article("Test Title", "Test Desc", "Test Body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_authentication_exception_when_creating_article_unauthenticated() {
    SecurityContextHolder.clearContext();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("Test").body("Test").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_when_authorized() {
    setAuthenticated(user);
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(article));

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("New Desc")
            .body("New Body")
            .build();
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("old-title", changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_not_found_when_updating_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", changes));
  }

  @Test
  void should_throw_no_authorization_when_updating_others_article() {
    setAuthenticated(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("title", changes));
  }

  @Test
  void should_favorite_article() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_not_found_when_favoriting_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("missing"));
  }

  @Test
  void should_throw_authentication_when_favoriting_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new org.springframework.security.authentication.AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  void should_unfavorite_article() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_when_authorized() {
    setAuthenticated(user);
    Article article = new Article("Title", "Desc", "Body", Collections.emptyList(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("missing"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_others_article() {
    setAuthenticated(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "Desc", "Body", Collections.emptyList(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
