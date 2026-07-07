package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class CommentDatafetcherTest {

  private CommentQueryService commentQueryService;
  private CommentDatafetcher commentDatafetcher;
  private MockedStatic<SecurityUtil> securityUtil;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService = Mockito.mock(CommentQueryService.class);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("test@example.com", "tester", "123", "", "");
    securityUtil = Mockito.mockStatic(SecurityUtil.class);
    securityUtil.when(SecurityUtil::getCurrentUser).thenReturn(java.util.Optional.of(user));
  }

  @AfterEach
  public void tearDown() {
    securityUtil.close();
  }

  private CommentData commentData(String id) {
    DateTime now = new DateTime();
    return new CommentData(
        id,
        "body-" + id,
        "article-1",
        now,
        now,
        new ProfileData("uid", "author", "bio", "img", false));
  }

  @Test
  public void should_get_comment() {
    CommentData comment = commentData("c1");
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(comment);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result.getData());
    assertEquals("c1", result.getData().getId());
  }

  @Test
  public void should_get_article_comments_with_first() {
    CommentData comment = commentData("c1");
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(comment), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("slug-1").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug-1", articleData());
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_article_comments_with_last() {
    CursorPager<CommentData> pager =
        new CursorPager<>(Collections.emptyList(), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("a1"), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("slug-1").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("slug-1", articleData());
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);

    assertNotNull(result.getData());
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_no_first_and_last() {
    DgsDataFetchingEnvironment dfe = Mockito.mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  private ArticleData articleData() {
    DateTime now = new DateTime();
    return new ArticleData(
        "a1",
        "slug-1",
        "title",
        "desc",
        "body",
        false,
        0,
        now,
        now,
        Collections.emptyList(),
        new ProfileData("uid", "author", "bio", "img", false));
  }
}
