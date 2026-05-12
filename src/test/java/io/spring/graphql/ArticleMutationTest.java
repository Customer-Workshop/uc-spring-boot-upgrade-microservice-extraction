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
  void should_create_article_with_null_taglist() {
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("Title").description("desc").body("body").build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  void should_throw_when_creating_article_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("T").description("d").body("b").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  void should_update_article_success() {
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder()
            .title("New Title")
            .description("new desc")
            .body("new body")
            .build();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), changes);

    assertNotNull(result);
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  void should_throw_when_updating_nonexistent_article() {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("t").build();

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("non-existent", changes));
  }

  @Test
  void should_throw_when_updating_article_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other Title", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));

    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("t").build();
    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(otherArticle.getSlug(), changes));
  }

  @Test
  void should_favorite_article_success() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

    assertNotNull(result);
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  void should_throw_when_favoriting_nonexistent_article() {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("non-existent"));
  }

  @Test
  void should_throw_when_favoriting_unauthenticated() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
    assertThrows(AuthenticationException.class, () -> articleMutation.favoriteArticle("slug"));
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
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  void should_delete_article_success() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus result = articleMutation.deleteArticle(article.getSlug());

    assertTrue(result.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  void should_throw_when_deleting_nonexistent_article() {
    when(articleRepository.findBySlug(anyString())).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("non-existent"));
  }

  @Test
  void should_throw_when_deleting_article_not_authorized() {
    User anotherUser = new User("other@test.com", "other", "pass", "", "");
    Article otherArticle =
        new Article("Other", "desc", "body", Arrays.asList("java"), anotherUser.getId());
    when(articleRepository.findBySlug(eq(otherArticle.getSlug())))
        .thenReturn(Optional.of(otherArticle));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.deleteArticle(otherArticle.getSlug()));
  }
}
