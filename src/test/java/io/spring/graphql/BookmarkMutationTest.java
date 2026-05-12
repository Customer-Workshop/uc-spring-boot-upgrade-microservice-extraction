package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.ResourceNotFoundException;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.ArticlePayload;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class BookmarkMutationTest {

  private ArticleBookmarkRepository articleBookmarkRepository;
  private ArticleRepository articleRepository;
  private BookmarkMutation bookmarkMutation;
  private User user;
  private Article article;

  @BeforeEach
  void setUp() {
    articleBookmarkRepository = mock(ArticleBookmarkRepository.class);
    articleRepository = mock(ArticleRepository.class);
    bookmarkMutation = new BookmarkMutation(articleBookmarkRepository, articleRepository);
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    article = new Article("Title", "desc", "body", Arrays.asList("java"), user.getId());
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anon", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  void should_bookmark_article_success() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    DataFetcherResult<ArticlePayload> result = bookmarkMutation.bookmarkArticle(article.getSlug());

    assertNotNull(result);
    verify(articleBookmarkRepository).save(any(ArticleBookmark.class));
  }

  @Test
  void should_throw_resource_not_found_when_bookmarking_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> bookmarkMutation.bookmarkArticle("nonexistent"));
  }

  @Test
  void should_throw_authentication_when_bookmarking_without_login() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> bookmarkMutation.bookmarkArticle(article.getSlug()));
  }

  @Test
  void should_unbookmark_article_success() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    ArticleBookmark bookmark = new ArticleBookmark(article.getId(), user.getId());
    when(articleBookmarkRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.of(bookmark));

    DataFetcherResult<ArticlePayload> result =
        bookmarkMutation.unbookmarkArticle(article.getSlug());

    assertNotNull(result);
    verify(articleBookmarkRepository).remove(eq(bookmark));
  }

  @Test
  void should_unbookmark_article_when_not_bookmarked() {
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    when(articleBookmarkRepository.find(article.getId(), user.getId()))
        .thenReturn(Optional.empty());

    DataFetcherResult<ArticlePayload> result =
        bookmarkMutation.unbookmarkArticle(article.getSlug());

    assertNotNull(result);
    verify(articleBookmarkRepository, never()).remove(any());
  }

  @Test
  void should_throw_resource_not_found_when_unbookmarking_nonexistent_article() {
    when(articleRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());
    assertThrows(
        ResourceNotFoundException.class, () -> bookmarkMutation.unbookmarkArticle("nonexistent"));
  }

  @Test
  void should_throw_authentication_when_unbookmarking_without_login() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class, () -> bookmarkMutation.unbookmarkArticle(article.getSlug()));
  }
}
