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

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  void should_create_article_successfully() {
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
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_taglist() {
    authenticateUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .build();
    Article article =
        new Article("Test Title", "Test Desc", "Test Body", Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);
    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_authentication_exception_when_creating_article_unauthenticated() {
    setAnonymous();
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("Desc").body("Body").build();
    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_successfully() {
    authenticateUser(user);
    Article article =
        new Article("Old Title", "Old Desc", "Old Body", Arrays.asList(), user.getId());
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), input);
    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_not_found_when_updating_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New Title").build();
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("nonexistent", input));
  }

  @Test
  void should_throw_no_authorization_when_updating_other_users_article() {
    authenticateUser(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("New").build();
    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), input));
  }

  @Test
  void should_favorite_article_successfully() {
    authenticateUser(user);
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_not_found_when_favoriting_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  void should_throw_auth_exception_when_favoriting_unauthenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("some-slug"));
  }

  @Test
  void should_unfavorite_article_successfully() {
    authenticateUser(user);
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_when_no_existing_favorite() {
    authenticateUser(user);
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());
    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_successfully() {
    authenticateUser(user);
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_article() {
    authenticateUser(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_other_users_article() {
    authenticateUser(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article article = new Article("Title", "Desc", "Body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
  }

  @Test
  void should_throw_auth_exception_when_deleting_unauthenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("some-slug"));
  }
}
