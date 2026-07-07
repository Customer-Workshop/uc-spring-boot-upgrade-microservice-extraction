package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class ArticleMutationTest {

  private ArticleCommandService articleCommandService;
  private ArticleFavoriteRepository articleFavoriteRepository;
  private ArticleRepository articleRepository;
  private ArticleMutation articleMutation;

  private MockedStatic<SecurityUtil> securityUtil;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    articleCommandService = Mockito.mock(ArticleCommandService.class);
    articleFavoriteRepository = Mockito.mock(ArticleFavoriteRepository.class);
    articleRepository = Mockito.mock(ArticleRepository.class);
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);

    user = new User("test@example.com", "tester", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());

    securityUtil = Mockito.mockStatic(SecurityUtil.class);
  }

  @AfterEach
  public void tearDown() {
    securityUtil.close();
  }

  @Test
  public void should_create_article() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result.getData());
    assertTrue(result.getLocalContext() instanceof Article);
    verify(articleCommandService).createArticle(any(), eq(user));
  }

  @Test
  public void should_create_article_with_null_tag_list() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("title").description("desc").body("body").build();
    when(articleCommandService.createArticle(any(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    assertNotNull(result.getData());
  }

  @Test
  public void should_throw_when_create_article_unauthenticated() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
    CreateArticleInput input = CreateArticleInput.newBuilder().title("title").build();

    assertThrows(AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any())).thenReturn(article);
    UpdateArticleInput changes =
        UpdateArticleInput.newBuilder().title("new").body("new").description("new").build();

    DataFetcherResult<ArticlePayload> result = articleMutation.updateArticle("title", changes);

    assertNotNull(result.getData());
    verify(articleCommandService).updateArticle(eq(article), any());
  }

  @Test
  public void should_throw_when_update_article_not_found() {
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("new").build();

    assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("missing", changes));
  }

  @Test
  public void should_throw_when_update_article_not_author() {
    User other = new User("other@example.com", "other", "123", "", "");
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(other));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    UpdateArticleInput changes = UpdateArticleInput.newBuilder().title("new").build();

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.updateArticle("title", changes));
  }

  @Test
  public void should_favorite_article() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle("title");

    assertNotNull(result.getData());
    verify(articleFavoriteRepository).save(any(ArticleFavorite.class));
  }

  @Test
  public void should_throw_when_favorite_article_not_found() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    when(articleRepository.findBySlug("missing")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("missing"));
  }

  @Test
  public void should_unfavorite_article_when_favorite_present() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(favorite));

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result.getData());
    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_unfavorite_article_when_favorite_absent() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result = articleMutation.unfavoriteArticle("title");

    assertNotNull(result.getData());
    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  public void should_delete_article() {
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    DeletionStatus status = articleMutation.deleteArticle("title");

    assertTrue(status.getSuccess());
    verify(articleRepository, times(1)).remove(article);
  }

  @Test
  public void should_throw_when_delete_article_not_author() {
    User other = new User("other@example.com", "other", "123", "", "");
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(other));
    when(articleRepository.findBySlug("title")).thenReturn(Optional.of(article));

    assertThrows(NoAuthorizationException.class, () -> articleMutation.deleteArticle("title"));
  }
}
