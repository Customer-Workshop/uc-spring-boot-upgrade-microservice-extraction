package io.spring.graphql;

import static java.util.Arrays.asList;
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
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "description", "body", asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticatedUser(User user) {
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  private void setAnonymousUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  void createArticle_success() {
    setAuthenticatedUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("desc")
            .body("body content")
            .tagList(asList("java", "spring"))
            .build();

    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(article, result.getLocalContext());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void createArticle_withNullTagList_usesEmptyList() {
    setAuthenticatedUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("desc")
            .body("body content")
            .build();

    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void createArticle_withoutAuth_throwsAuthenticationException() {
    setAnonymousUser();
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("desc")
            .body("body content")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void updateArticle_success() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("updated body")
            .description("updated desc")
            .build();
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("test-title", changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void updateArticle_notFound_throwsResourceNotFoundException() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("Updated").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", changes));
  }

  @Test
  void updateArticle_withoutAuth_throwsAuthenticationException() {
    setAnonymousUser();
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("Updated").build();

    assertThrows(
        AuthenticationException.class, () -> articleMutation.updateArticle("test-title", changes));
  }

  @Test
  void updateArticle_notAuthor_throwsNoAuthorizationException() {
    User otherUser = new User("other@example.com", "other", "password", "", "");
    setAuthenticatedUser(otherUser);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("Updated").build();

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("test-title", changes));
  }

  @Test
  void favoriteArticle_success() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
    assertEquals(article, result.getLocalContext());
  }

  @Test
  void favoriteArticle_notFound_throwsResourceNotFoundException() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  void favoriteArticle_withoutAuth_throwsAuthenticationException() {
    setAnonymousUser();

    assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle("test-title"));
  }

  @Test
  void unfavoriteArticle_success() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(favorite);
    assertEquals(article, result.getLocalContext());
  }

  @Test
  void unfavoriteArticle_noFavoriteExists_doesNotRemove() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void unfavoriteArticle_withoutAuth_throwsAuthenticationException() {
    setAnonymousUser();

    assertThrows(
        AuthenticationException.class, () -> articleMutation.unfavoriteArticle("test-title"));
  }

  @Test
  void deleteArticle_success() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-title");

    assertNotNull(result);
    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void deleteArticle_notFound_throwsResourceNotFoundException() {
    setAuthenticatedUser(user);
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  void deleteArticle_notAuthor_throwsNoAuthorizationException() {
    User otherUser = new User("other@example.com", "other", "password", "", "");
    setAuthenticatedUser(otherUser);
    when(articleRepository.findBySlug("test-title")).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("test-title"));
  }

  @Test
  void deleteArticle_withoutAuth_throwsAuthenticationException() {
    setAnonymousUser();

    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("test-title"));
  }
}
