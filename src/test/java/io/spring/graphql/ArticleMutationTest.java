package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.application.article.NewArticleParam;
import io.spring.application.article.UpdateArticleParam;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    article = new Article("Test Title", "description", "body", Arrays.asList("java"), user.getId());
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(u, null));
  }

  @Test
  void should_create_article_success() {
    setAuthenticated(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertTrue(result.getData() instanceof ArticlePayload);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  @Test
  void should_create_article_with_null_tag_list() {
    setAuthenticated(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Test Title")
            .description("description")
            .body("body")
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(NewArticleParam.class), eq(user));
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
  }

  @Test
  void should_throw_authentication_when_creating_article_unauthenticated() {
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
  void should_update_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("New Body")
            .description("New Desc")
            .build();
    var result = articleMutation.updateArticle("test-title", changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any(UpdateArticleParam.class));
  }

  @Test
  void should_throw_not_found_when_updating_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("non-existent", changes));
  }

  @Test
  void should_throw_no_authorization_when_updating_other_users_article() {
    setAuthenticated(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("New Title").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("other-title", changes));
  }

  @Test
  void should_favorite_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    var result = articleMutation.favoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_authentication_when_favoriting_unauthenticated() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle("test-title"));
  }

  @Test
  void should_throw_not_found_when_favoriting_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("non-existent"));
  }

  @Test
  void should_unfavorite_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    var result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_when_not_previously_favorited() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    var result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_success() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_no_authorization_when_deleting_other_users_article() {
    setAuthenticated(user);
    User otherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList(), otherUser.getId());
    when(articleRepository.findBySlug(eq("other-title"))).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("other-title"));
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_article() {
    setAuthenticated(user);
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("non-existent"));
  }

  @Test
  void should_throw_authentication_when_deleting_unauthenticated() {
    setAnonymous();
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("test-title"));
  }
}
