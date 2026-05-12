package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.security.authentication.TestingAuthenticationToken;
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
            .title("Test Title")
            .description("desc")
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
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("desc").body("body").build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
  }

  @Test
  void should_throw_when_unauthenticated_create_article() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Test").description("desc").body("body").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    UpdateArticleInput params =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .body("new body")
            .description("new desc")
            .build();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), params);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_not_found_when_update_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("nonexistent", params));
  }

  @Test
  void should_throw_no_authorization_when_update_other_user_article() {
    User other = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList("java"), other.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));

    UpdateArticleInput params = UpdateArticleInput.newBuilder().title("New").build();

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(otherArticle.getSlug(), params));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_unauthenticated_favorite() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("some-slug"));
  }

  @Test
  void should_throw_not_found_when_favorite_nonexistent() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("nonexistent"));
  }

  @Test
  void should_unfavorite_article_success() {
    ArticleFavorite fav = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(fav));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).remove(fav);
  }

  @Test
  void should_unfavorite_article_when_no_favorite_exists() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle(article.getSlug());

    assertNotNull(result);
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_not_found_when_delete_nonexistent() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("nonexistent"));
  }

  @Test
  void should_throw_no_authorization_when_delete_other_user_article() {
    User other = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList("java"), other.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.deleteArticle(otherArticle.getSlug()));
  }

  @Test
  void should_throw_when_unauthenticated_delete() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> articleMutation.deleteArticle("some-slug"));
  }
}
