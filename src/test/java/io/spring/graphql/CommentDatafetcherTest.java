package io.spring.graphql;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import graphql.execution.DataFetcherResult;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
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
import java.util.Collections;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest extends GraphQLTestBase {

  @Mock private CommentQueryService commentQueryService;

  private CommentDatafetcher commentDatafetcher;
  private User user;
  private ArticleData articleData;
  private CommentData commentData;

  @BeforeEach
  public void setUp() {
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
    user = new User("a@test.com", "a", "123", "", "");
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), "bio", "image", false);
    articleData =
        new ArticleData(
            "article-id",
            "slug",
            "title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Collections.emptyList(),
            profileData);
    commentData =
        new CommentData(
            "comment-id", "content", "article-id", new DateTime(), new DateTime(), profileData);
  }

  private DgsDataFetchingEnvironment articleEnvironment() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("slug").build());
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("slug", articleData));
    return dfe;
  }

  @Test
  public void should_get_comment_from_local_context() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    Assertions.assertEquals("comment-id", result.getData().getId());
    Assertions.assertEquals("content", result.getData().getBody());
    Assertions.assertTrue(((Map<?, ?>) result.getLocalContext()).containsKey(commentData.getId()));
  }

  @Test
  public void should_get_article_comments_forward() {
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(null), any()))
        .thenReturn(new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, articleEnvironment());

    Assertions.assertEquals(1, result.getData().getEdges().size());
    Assertions.assertEquals("comment-id", result.getData().getEdges().get(0).getNode().getId());
    Assertions.assertFalse(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  public void should_get_article_comments_backward_with_cursor() {
    setCurrentUser(user);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(new CursorPager<>(Arrays.asList(commentData), Direction.PREV, true));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(
            null, null, 10, String.valueOf(new DateTime().getMillis()), articleEnvironment());

    Assertions.assertTrue(result.getData().getPageInfo().isHasPreviousPage());
    ArgumentCaptor<CursorPageParameter<DateTime>> captor =
        ArgumentCaptor.forClass(CursorPageParameter.class);
    verify(commentQueryService).findByArticleIdWithCursor(any(), any(), captor.capture());
    Assertions.assertEquals(Direction.PREV, captor.getValue().getDirection());
    Assertions.assertNotNull(captor.getValue().getCursor());
  }

  @Test
  public void should_return_empty_page_info_for_no_comments() {
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(null), any()))
        .thenReturn(new CursorPager<>(new ArrayList<>(), Direction.NEXT, false));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, articleEnvironment());

    Assertions.assertTrue(result.getData().getEdges().isEmpty());
    Assertions.assertNull(result.getData().getPageInfo().getStartCursor());
  }

  @Test
  public void should_reject_missing_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Assertions.assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
