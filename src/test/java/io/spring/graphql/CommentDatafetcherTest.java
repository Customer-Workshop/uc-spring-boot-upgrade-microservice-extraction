package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest extends GraphQLTestBase {

  @Mock private CommentQueryService commentQueryService;

  @Mock private DgsDataFetchingEnvironment dfe;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;
  private CommentData commentData;
  private ArticleData articleData;

  @BeforeEach
  public void setUp() {
    user = new User("john@jacob.com", "johnjacob", "123", "bio", "image");
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), "bio", "image", false);
    commentData =
        new CommentData(
            "comment-id", "content", "article-id", new DateTime(), new DateTime(), profileData);
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
            Arrays.asList("java"),
            profileData);
  }

  @Test
  public void should_get_comment_from_local_context() {
    when(dfe.<CommentData>getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertEquals("comment-id", result.getData().getId());
    assertEquals("content", result.getData().getBody());
    Map<String, Object> localContext = (Map<String, Object>) result.getLocalContext();
    assertEquals(commentData, localContext.get("comment-id"));
  }

  @Test
  public void should_get_article_comments_forward() {
    anonymous();
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("slug").build());
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("slug", articleData));
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), isNull(), any()))
        .thenReturn(
            new CursorPager<>(new ArrayList<>(Arrays.asList(commentData)), Direction.NEXT, true));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertEquals(1, result.getData().getEdges().size());
    assertEquals("comment-id", result.getData().getEdges().get(0).getNode().getId());
    assertTrue(result.getData().getPageInfo().isHasNextPage());
  }

  @Test
  public void should_get_article_comments_backward_with_cursor() {
    authenticate(user);
    when(dfe.<Article>getSource()).thenReturn(Article.newBuilder().slug("slug").build());
    when(dfe.<Map<String, ArticleData>>getLocalContext())
        .thenReturn(Collections.singletonMap("slug", articleData));
    ArgumentCaptor<CursorPageParameter<DateTime>> captor =
        ArgumentCaptor.forClass(CursorPageParameter.class);
    when(commentQueryService.findByArticleIdWithCursor(
            eq("article-id"), eq(user), captor.capture()))
        .thenReturn(new CursorPager<>(new ArrayList<>(), Direction.PREV, false));

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, "2000", dfe);

    assertTrue(result.getData().getEdges().isEmpty());
    assertFalse(result.getData().getPageInfo().isHasPreviousPage());
    assertEquals(Direction.PREV, captor.getValue().getDirection());
    assertEquals(2000L, captor.getValue().getCursor().getMillis());
  }

  @Test
  public void should_reject_article_comments_without_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
