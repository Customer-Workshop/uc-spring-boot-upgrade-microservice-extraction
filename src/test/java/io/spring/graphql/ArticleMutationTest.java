package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.api.exception.NoAuthorizationException;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.application.article.ArticleCommandService;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.favorite.ArticleFavorite;
import io.spring.core.favorite.ArticleFavoriteRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
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
import org.mockito.InjectMocks;
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

  @InjectMocks private ArticleMutation articleMutation;

  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    article = new Article("Test Title", "desc", "body", Arrays.asList("java"), user.getId());
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_create_article_success() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("description")
            .body("body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    Article created =
        new Article(
            "New Article",
            "description",
            "body content",
            Arrays.asList("java", "spring"),
            user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(created);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_create_article_with_null_tagList() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("description")
            .body("body content")
            .build();

    Article created =
        new Article("New Article", "description", "body content", Arrays.asList(), user.getId());
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(created);

    var result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_authentication_exception_when_creating_article_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("New Article")
            .description("description")
            .body("body content")
            .build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("Updated Title")
            .body("Updated Body")
            .description("Updated Desc")
            .build();

    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    var result = articleMutation.updateArticle("test-title", params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_not_found_when_updating_nonexistent_article() {
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", params));
  }

  @Test
  void should_throw_no_authorization_when_updating_others_article() {
    User otherUser = new User("other@test.com", "otheruser", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());

    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("Updated Title").build();

    when(articleRepository.findBySlug(eq("other-article"))).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle("other-article", params));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    var result = articleMutation.favoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_authentication_when_favoriting_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle("test-title"));
  }

  @Test
  void should_throw_not_found_when_favoriting_nonexistent_article() {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  void should_unfavorite_article_success() {
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    var result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  void should_unfavorite_article_when_not_favorited() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    var result = articleMutation.unfavoriteArticle("test-title");

    assertNotNull(result);
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_throw_authentication_when_unfavoriting_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(
        AuthenticationException.class, () -> articleMutation.unfavoriteArticle("test-title"));
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug(eq("test-title"))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle("test-title");

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_authentication_when_deleting_without_login() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key",
                "anonymous",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("test-title"));
  }

  @Test
  void should_throw_not_found_when_deleting_nonexistent_article() {
    when(articleRepository.findBySlug(eq("nonexistent"))).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  void should_throw_no_authorization_when_deleting_others_article() {
    User otherUser = new User("other@test.com", "otheruser", "pass", "", "");
    Article otherArticle =
        new Article("Other Article", "desc", "body", Arrays.asList(), otherUser.getId());

    when(articleRepository.findBySlug(eq("other-article"))).thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle("other-article"));
  }
}
