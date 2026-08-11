package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest extends GraphQLTestBase {

  @Mock private ArticleCommandService articleCommandService;
  @Mock private ArticleFavoriteRepository articleFavoriteRepository;
  @Mock private ArticleRepository articleRepository;

  private ArticleMutation articleMutation;
  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    articleMutation =
        new ArticleMutation(articleCommandService, articleFavoriteRepository, articleRepository);
    user = new User("a@test.com", "a", "123", "", "");
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  @Test
  public void should_create_article_success() {
    setCurrentUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java"))
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result = articleMutation.createArticle(input);

    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    verify(articleCommandService).createArticle(captor.capture(), eq(user));
    Assertions.assertEquals(Arrays.asList("java"), captor.getValue().getTagList());
    Assertions.assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_create_article_with_empty_tag_list() {
    setCurrentUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder().title("title").description("desc").body("body").build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    articleMutation.createArticle(input);

    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    verify(articleCommandService).createArticle(captor.capture(), eq(user));
    Assertions.assertTrue(captor.getValue().getTagList().isEmpty());
  }

  @Test
  public void should_create_article_with_special_characters() {
    setCurrentUser(user);
    CreateArticleInput input =
        CreateArticleInput.newBuilder()
            .title("Emoji 🚀 & «quotes» — 日本語")
            .description("<script>alert(\"x\")</script>")
            .body("line1\nline2\ttabbed \\ 100%")
            .tagList(Arrays.asList("c++", "c#", ".net"))
            .build();
    when(articleCommandService.createArticle(any(NewArticleParam.class), eq(user)))
        .thenReturn(article);

    articleMutation.createArticle(input);

    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    verify(articleCommandService).createArticle(captor.capture(), eq(user));
    Assertions.assertEquals("Emoji 🚀 & «quotes» — 日本語", captor.getValue().getTitle());
    Assertions.assertEquals("<script>alert(\"x\")</script>", captor.getValue().getDescription());
    Assertions.assertEquals("line1\nline2\ttabbed \\ 100%", captor.getValue().getBody());
    Assertions.assertEquals(Arrays.asList("c++", "c#", ".net"), captor.getValue().getTagList());
  }

  @Test
  public void should_not_create_article_without_login() {
    setAnonymousUser();
    CreateArticleInput input = CreateArticleInput.newBuilder().title("title").build();

    Assertions.assertThrows(
        AuthenticationException.class, () -> articleMutation.createArticle(input));
  }

  @Test
  public void should_update_article_success() {
    setCurrentUser(user);
    UpdateArticleInput input =
        UpdateArticleInput.newBuilder()
            .title("new title")
            .body("new body")
            .description("new desc")
            .build();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), input);

    Assertions.assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_not_update_unknown_article() {
    when(articleRepository.findBySlug(eq("unknown"))).thenReturn(Optional.empty());
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("new title").build();

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.updateArticle("unknown", input));
  }

  @Test
  public void should_not_update_article_without_login() {
    setAnonymousUser();
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("new title").build();

    Assertions.assertThrows(
        AuthenticationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), input));
  }

  @Test
  public void should_not_update_article_of_other_users() {
    setCurrentUser(new User("b@test.com", "b", "123", "", ""));
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    UpdateArticleInput input = UpdateArticleInput.newBuilder().title("new title").build();

    Assertions.assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), input));
    verify(articleCommandService, never()).updateArticle(any(), any());
  }

  @Test
  public void should_favorite_article_success() {
    setCurrentUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

    verify(articleFavoriteRepository).save(new ArticleFavorite(article.getId(), user.getId()));
    Assertions.assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_not_favorite_article_without_login() {
    setAnonymousUser();

    Assertions.assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle(article.getSlug()));
  }

  @Test
  public void should_not_favorite_unknown_article() {
    setCurrentUser(user);
    when(articleRepository.findBySlug(eq("unknown"))).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("unknown"));
  }

  @Test
  public void should_unfavorite_article_success() {
    setCurrentUser(user);
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    articleMutation.unfavoriteArticle(article.getSlug());

    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_ignore_unfavorite_when_favorite_is_missing() {
    setCurrentUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    articleMutation.unfavoriteArticle(article.getSlug());

    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  public void should_not_unfavorite_article_without_login() {
    setAnonymousUser();

    Assertions.assertThrows(
        AuthenticationException.class, () -> articleMutation.unfavoriteArticle(article.getSlug()));
  }

  @Test
  public void should_delete_article_success() {
    setCurrentUser(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus status = articleMutation.deleteArticle(article.getSlug());

    Assertions.assertTrue(status.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  public void should_not_delete_article_of_other_users() {
    setCurrentUser(new User("b@test.com", "b", "123", "", ""));
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    Assertions.assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
    verify(articleRepository, never()).remove(any());
  }

  @Test
  public void should_not_delete_unknown_article() {
    setCurrentUser(user);
    when(articleRepository.findBySlug(eq("unknown"))).thenReturn(Optional.empty());

    Assertions.assertThrows(
        ResourceNotFoundException.class, () -> articleMutation.deleteArticle("unknown"));
  }

  @Test
  public void should_not_delete_article_without_login() {
    setAnonymousUser();

    Assertions.assertThrows(
        AuthenticationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
  }
}
