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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation mutation;
  private User user;

  @BeforeEach
  void setUp() {
    mutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_article() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(List.of("java"))
            .build();

    Article article = new Article("Test Title", "desc", "body", List.of("java"), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = mutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .build();

    Article article = new Article("Test Title", "desc", "body", List.of(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = mutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_throw_when_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Title").description("desc").body("body").build();

    assertThrows(AuthenticationException.class, () -> mutation.createArticle(input));
  }

  @Test
  void should_update_article() {
    Article article = new Article("Old Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(article));

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("new body")
            .description("new desc")
            .build();

    Article updatedArticle =
        new Article("New Title", "new desc", "new body", List.of(), user.getId());
    when(articleCommandService.updateArticle(any(), any())).thenReturn(updatedArticle);

    DataFetcherResult<ArticlePayload> result = mutation.updateArticle("old-title", params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(any(), any());
  }

  @Test
  void should_throw_when_update_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(ResourceNotFoundException.class, () -> mutation.updateArticle("missing", params));
  }

  @Test
  void should_throw_when_update_article_no_authorization() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", List.of(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(NoAuthorizationException.class, () -> mutation.updateArticle("title", params));
  }

  @Test
  void should_favorite_article() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = mutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_favorite_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> mutation.favoriteArticle("slug"));
  }

  @Test
  void should_throw_when_favorite_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> mutation.favoriteArticle("missing"));
  }

  @Test
  void should_unfavorite_article() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = mutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = mutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article() {
    Article article = new Article("Title", "desc", "body", List.of(), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus result = mutation.deleteArticle("title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_when_delete_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> mutation.deleteArticle("slug"));
  }

  @Test
  void should_throw_when_delete_no_authorization() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "desc", "body", List.of(), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> mutation.deleteArticle("title"));
  }
}
