package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import io.spring.graphql.types.UpdateArticleInput;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ArticleMutationTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();
  private final Article article = GraphqlTestFixtures.article(user);

  @Test
  void createsArticleForAuthenticatedUser() {
    ArticleCommandService command = mock(ArticleCommandService.class);
    when(command.createArticle(any(), eq(user))).thenReturn(article);
    ArticleMutation mutation =
        new ArticleMutation(
            command, mock(ArticleFavoriteRepository.class), mock(ArticleRepository.class));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(
          article,
          mutation
              .createArticle(CreateArticleInput.newBuilder().title("new").build())
              .getLocalContext());
    }
  }

  @Test
  void rejectsUnauthenticatedCreate() {
    ArticleMutation mutation =
        new ArticleMutation(
            mock(ArticleCommandService.class),
            mock(ArticleFavoriteRepository.class),
            mock(ArticleRepository.class));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertThrows(
          AuthenticationException.class,
          () -> mutation.createArticle(CreateArticleInput.newBuilder().build()));
    }
  }

  @Test
  void updatesAndDeletesOnlyOwnedArticle() {
    ArticleCommandService command = mock(ArticleCommandService.class);
    ArticleRepository articles = mock(ArticleRepository.class);
    when(articles.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    when(command.updateArticle(eq(article), any())).thenReturn(article);
    ArticleMutation mutation =
        new ArticleMutation(command, mock(ArticleFavoriteRepository.class), articles);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertEquals(
          article,
          mutation
              .updateArticle(
                  article.getSlug(), UpdateArticleInput.newBuilder().title("new").build())
              .getLocalContext());
      assertTrue(mutation.deleteArticle(article.getSlug()).getSuccess());
      verify(articles).remove(article);
    }
    User other = GraphqlTestFixtures.user();
    Article otherArticle = GraphqlTestFixtures.article(other);
    when(articles.findBySlug(otherArticle.getSlug())).thenReturn(Optional.of(otherArticle));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      assertThrows(
          NoAuthorizationException.class, () -> mutation.deleteArticle(otherArticle.getSlug()));
    }
  }

  @Test
  void favoritesHandleMissingArticlesAndExistingFavorites() {
    ArticleFavoriteRepository favorites = mock(ArticleFavoriteRepository.class);
    ArticleRepository articles = mock(ArticleRepository.class);
    when(articles.findBySlug(article.getSlug())).thenReturn(Optional.of(article));
    ArticleFavorite favorite = new ArticleFavorite(article.getId(), user.getId());
    when(favorites.find(article.getId(), user.getId())).thenReturn(Optional.of(favorite));
    ArticleMutation mutation =
        new ArticleMutation(mock(ArticleCommandService.class), favorites, articles);
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.of(user));
      mutation.favoriteArticle(article.getSlug());
      mutation.unfavoriteArticle(article.getSlug());
      verify(favorites).save(any());
      verify(favorites).remove(favorite);
      assertThrows(ResourceNotFoundException.class, () -> mutation.favoriteArticle("missing"));
    }
  }
}
