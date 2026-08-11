package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleMutationTest extends GraphQLTestBase {

  @Mock private ArticleCommandService articleCommandService;

  @Mock private ArticleFavoriteRepository articleFavoriteRepository;

  @Mock private ArticleRepository articleRepository;

  @InjectMocks private ArticleMutation articleMutation;

  private User user;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    article = new Article("title", "desc", "body", Arrays.asList("java"), user.getId());
  }

  private CreateArticleInput createArticleInput(java.util.List<String> tagList) {
    CreateArticleInput input = new CreateArticleInput();
    input.setTitle("title");
    input.setDescription("desc");
    input.setBody("body");
    input.setTagList(tagList);
    return input;
  }

  @Test
  public void should_create_article() {
    authenticate(user);
    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    when(articleCommandService.createArticle(captor.capture(), eq(user))).thenReturn(article);

    DataFetcherResult<ArticlePayload> result =
        articleMutation.createArticle(createArticleInput(Arrays.asList("java")));

    assertEquals(article, result.getLocalContext());
    assertEquals(Arrays.asList("java"), captor.getValue().getTagList());
  }

  @Test
  public void should_default_tag_list_to_empty_when_absent() {
    authenticate(user);
    ArgumentCaptor<NewArticleParam> captor = ArgumentCaptor.forClass(NewArticleParam.class);
    when(articleCommandService.createArticle(captor.capture(), eq(user))).thenReturn(article);

    articleMutation.createArticle(createArticleInput(null));

    assertTrue(captor.getValue().getTagList().isEmpty());
  }

  @Test
  public void should_not_create_article_for_anonymous_user() {
    anonymous();

    assertThrows(
        AuthenticationException.class,
        () -> articleMutation.createArticle(createArticleInput(null)));
    verify(articleCommandService, never()).createArticle(any(), any());
  }

  @Test
  public void should_update_article_of_current_user() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleCommandService.updateArticle(eq(article), any(UpdateArticleParam.class)))
        .thenReturn(article);
    UpdateArticleInput input = new UpdateArticleInput();
    input.setTitle("new title");
    input.setBody("new body");
    input.setDescription("new desc");

    DataFetcherResult<ArticlePayload> result =
        articleMutation.updateArticle(article.getSlug(), input);

    assertEquals(article, result.getLocalContext());
  }

  @Test
  public void should_not_update_article_of_other_user() {
    User other = new User("other@test.com", "other", "123", "", "");
    authenticate(other);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class,
        () -> articleMutation.updateArticle(article.getSlug(), new UpdateArticleInput()));
  }

  @Test
  public void should_not_update_unknown_article() {
    when(articleRepository.findBySlug(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> articleMutation.updateArticle("ghost", new UpdateArticleInput()));
  }

  @Test
  public void should_favorite_article() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = articleMutation.favoriteArticle(article.getSlug());

    assertEquals(article, result.getLocalContext());
    verify(articleFavoriteRepository).save(new ArticleFavorite(article.getId(), user.getId()));
  }

  @Test
  public void should_not_favorite_unknown_article() {
    authenticate(user);
    when(articleRepository.findBySlug(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.favoriteArticle("ghost"));
  }

  @Test
  public void should_not_favorite_article_for_anonymous_user() {
    anonymous();

    assertThrows(
        AuthenticationException.class, () -> articleMutation.favoriteArticle(article.getSlug()));
  }

  @Test
  public void should_unfavorite_article() {
    authenticate(user);
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.of(favorite));

    articleMutation.unfavoriteArticle(article.getSlug());

    verify(articleFavoriteRepository).remove(favorite);
  }

  @Test
  public void should_ignore_unfavorite_when_favorite_is_absent() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleFavoriteRepository.find(eq(article.getId()), eq(user.getId())))
        .thenReturn(Optional.empty());

    articleMutation.unfavoriteArticle(article.getSlug());

    verify(articleFavoriteRepository, never()).remove(any());
  }

  @Test
  public void should_delete_own_article() {
    authenticate(user);
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DeletionStatus status = articleMutation.deleteArticle(article.getSlug());

    assertTrue(status.getSuccess());
    verify(articleRepository).remove(article);
  }

  @Test
  public void should_not_delete_article_of_other_user() {
    authenticate(new User("other@test.com", "other", "123", "", ""));
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    assertThrows(
        NoAuthorizationException.class, () -> articleMutation.deleteArticle(article.getSlug()));
    verify(articleRepository, never()).remove(any());
  }

  @Test
  public void should_not_delete_unknown_article() {
    authenticate(user);
    when(articleRepository.findBySlug(eq("ghost"))).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> articleMutation.deleteArticle("ghost"));
  }
}
