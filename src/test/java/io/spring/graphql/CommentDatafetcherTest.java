package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  private CommentDatafetcher fetcher;
  private User user;

  @BeforeEach
  void setUp() {
    fetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment_from_payload() {
    CommentData commentData = createCommentData("c1");
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = fetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("c1", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData = createArticleData("article1", "test-slug");
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData = createCommentData("c1");
    CursorPager<CommentData> pager = new CursorPager<>(List.of(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article1"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        fetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_last() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData = createArticleData("article1", "test-slug");
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CommentData commentData = createCommentData("c1");
    CursorPager<CommentData> pager = new CursorPager<>(List.of(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article1"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        fetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_article_comments_first_and_last_both_null() {
    assertThrows(
        IllegalArgumentException.class, () -> fetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_handle_empty_comments() {
    Article article = Article.newBuilder().slug("test-slug").build();
    when(dfe.getSource()).thenReturn(article);

    ArticleData articleData = createArticleData("article1", "test-slug");
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);
    when(dfe.getLocalContext()).thenReturn(map);

    CursorPager<CommentData> pager = new CursorPager<>(List.of(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article1"), any(), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        fetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  private CommentData createCommentData(String id) {
    return new CommentData(
        id,
        "comment body",
        "article1",
        new DateTime(),
        new DateTime(),
        new ProfileData("userId", "testuser", "bio", "image", false));
  }

  private ArticleData createArticleData(String id, String slug) {
    return new ArticleData(
        id,
        slug,
        "title",
        "description",
        "body",
        false,
        0,
        new DateTime(),
        new DateTime(),
        List.of("tag1"),
        new ProfileData("userId", "testuser", "bio", "image", false));
  }
}
