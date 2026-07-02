package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.ArticleData;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.graphql.types.Article;
import io.spring.graphql.types.Comment;
import io.spring.graphql.types.CommentsConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;
  private CommentData commentData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    setAnonymous();
    DateTime now = new DateTime();
    commentData =
        new CommentData(
            "comment-id",
            "test body",
            "article-id",
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAuthenticated(User u) {
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(u, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    AnonymousAuthenticationToken auth =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @Test
  void should_get_comment() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("comment-id", result.getData().getId());
    assertEquals("test body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    setAuthenticated(user);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article source = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(source);
    DateTime now = new DateTime();
    ArticleData artData =
        new ArticleData(
            "article-id",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", artData);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
    assertEquals("comment-id", result.getData().getEdges().get(0).getNode().getId());
  }

  @Test
  void should_get_article_comments_with_last() {
    setAuthenticated(user);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article source = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(source);
    DateTime now = new DateTime();
    ArticleData artData =
        new ArticleData(
            "article-id",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", artData);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_article_comments_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_empty() {
    setAuthenticated(user);
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    Article source = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(source);
    DateTime now = new DateTime();
    ArticleData artData =
        new ArticleData(
            "article-id",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            Arrays.asList("java"),
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", artData);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertTrue(result.getData().getEdges().isEmpty());
  }
}
