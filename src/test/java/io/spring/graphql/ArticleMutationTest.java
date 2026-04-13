package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class ArticleMutationTest {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;
  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() throws Exception {
    SecurityContextHolder.clearContext();
    closeable.close();
  }

  @Test
  void should_create_article() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article article =
        new Article("Test Title", "desc", "body", Arrays.asList("java", "spring"), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
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

    Article article =
        new Article("Test Title", "desc", "body", Collections.emptyList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_throw_when_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("d").body("b").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article() {
    Article article =
        new Article("Old Title", "old desc", "old body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("old-title")).thenReturn(Optional.of(article));

    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("new desc")
            .body("new body")
            .build();
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("old-title", params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_update_article_throw_when_not_author() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(
        io.spring.api.exception.NoAuthorizationException.class,
        () -> articleMutation.updateArticle("title", params));
  }

  @Test
  void should_favorite_article() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_favorite_article_throw_when_not_authenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
  }

  @Test
  void should_unfavorite_article() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  void should_unfavorite_article_when_no_existing_favorite() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article() {
    Article article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("title");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_delete_article_throw_when_not_author() {
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article =
        new Article("Title", "desc", "body", Arrays.asList("java"), otherUser.getId());
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(
        io.spring.api.exception.NoAuthorizationException.class,
        () -> articleMutation.deleteArticle("title"));
  }

  @Test
  void should_delete_article_throw_when_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(
        io.spring.api.exception.ResourceNotFoundException.class,
        () -> articleMutation.deleteArticle("missing"));
  }
}
