package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.spring.application.ArticleQueryService;
import io.spring.application.data.ArticleData;
import io.spring.application.data.ProfileData;
import io.spring.core.bookmark.ArticleBookmark;
import io.spring.core.bookmark.ArticleBookmarkRepository;
import io.spring.core.user.User;
import io.spring.graphql.exception.AuthenticationException;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.ArticlesConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class BookmarkDatafetcherTest {

  private ArticleBookmarkRepository articleBookmarkRepository;
  private ArticleQueryService articleQueryService;
  private BookmarkDatafetcher bookmarkDatafetcher;
  private User user;

  @BeforeEach
  void setUp() {
    articleBookmarkRepository = mock(ArticleBookmarkRepository.class);
    articleQueryService = mock(ArticleQueryService.class);
    bookmarkDatafetcher = new BookmarkDatafetcher(articleBookmarkRepository, articleQueryService);
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
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
  void should_get_bookmarked_articles_with_first() {
    ArticleData articleData =
        new ArticleData(
            "id1",
            "slug1",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            new ProfileData("uid", "author", "", "", false));
    when(articleBookmarkRepository.findBookmarkedArticleIds(user.getId()))
        .thenReturn(Arrays.asList("id1"));
    when(articleQueryService.findById(eq("id1"), eq(user))).thenReturn(Optional.of(articleData));

    DataFetcherResult<ArticlesConnection> result =
        bookmarkDatafetcher.getBookmarkedArticles(10, null, null, null);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("slug1", result.getData().getEdges().get(0).getNode().getSlug());
  }

  @Test
  void should_get_empty_bookmarked_articles() {
    when(articleBookmarkRepository.findBookmarkedArticleIds(user.getId()))
        .thenReturn(Collections.emptyList());

    DataFetcherResult<ArticlesConnection> result =
        bookmarkDatafetcher.getBookmarkedArticles(10, null, null, null);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }

  @Test
  void should_throw_authentication_when_not_logged_in() {
    setAnonymous();
    assertThrows(
        AuthenticationException.class,
        () -> bookmarkDatafetcher.getBookmarkedArticles(10, null, null, null));
  }

  @Test
  void should_check_bookmarked_status_when_logged_in() {
    Article article = Article.newBuilder().slug("test-slug").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(articleBookmarkRepository.find("test-slug", user.getId()))
        .thenReturn(Optional.of(new ArticleBookmark("test-slug", user.getId())));

    boolean result = bookmarkDatafetcher.isBookmarked(dfe);

    assertTrue(result);
  }

  @Test
  void should_return_false_bookmarked_when_not_bookmarked() {
    Article article = Article.newBuilder().slug("test-slug").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(articleBookmarkRepository.find("test-slug", user.getId())).thenReturn(Optional.empty());

    boolean result = bookmarkDatafetcher.isBookmarked(dfe);

    assertFalse(result);
  }

  @Test
  void should_return_false_bookmarked_when_not_logged_in() {
    setAnonymous();
    Article article = Article.newBuilder().slug("test-slug").build();
    DataFetchingEnvironment dfe = mock(DataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);

    boolean result = bookmarkDatafetcher.isBookmarked(dfe);

    assertFalse(result);
  }

  @Test
  void should_paginate_with_last_parameter() {
    ArticleData a1 =
        new ArticleData(
            "id1",
            "slug1",
            "title1",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            new ProfileData("uid", "author", "", "", false));
    ArticleData a2 =
        new ArticleData(
            "id2",
            "slug2",
            "title2",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            new ProfileData("uid", "author", "", "", false));
    when(articleBookmarkRepository.findBookmarkedArticleIds(user.getId()))
        .thenReturn(Arrays.asList("id1", "id2"));
    when(articleQueryService.findById(eq("id1"), eq(user))).thenReturn(Optional.of(a1));
    when(articleQueryService.findById(eq("id2"), eq(user))).thenReturn(Optional.of(a2));

    DataFetcherResult<ArticlesConnection> result =
        bookmarkDatafetcher.getBookmarkedArticles(null, null, 1, null);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }
}
