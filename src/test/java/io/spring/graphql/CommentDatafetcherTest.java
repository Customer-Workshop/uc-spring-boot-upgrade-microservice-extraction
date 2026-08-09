package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import io.spring.application.CommentQueryService;
import io.spring.application.CursorPager;
import io.spring.application.data.CommentData;
import io.spring.core.article.Article;
import io.spring.core.user.User;
import io.spring.graphql.types.Comment;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CommentDatafetcherTest extends GraphqlTestBase {
  private final User user = GraphqlTestFixtures.user();
  private final Article article = GraphqlTestFixtures.article(user);
  private final io.spring.core.comment.Comment comment = GraphqlTestFixtures.comment(article, user);
  private final CommentData data = GraphqlTestFixtures.commentData(comment, article, user);

  @Test
  void getsCommentPayload() {
    CommentDatafetcher fetcher = new CommentDatafetcher(mock(CommentQueryService.class));
    Comment result = fetcher.getComment(GraphqlTestFixtures.dgsEnvironment(null, data)).getData();
    assertEquals(comment.getId(), result.getId());
  }

  @Test
  void getsArticleCommentsAndRejectsInvalidPagination() {
    CommentQueryService service = mock(CommentQueryService.class);
    CommentDatafetcher fetcher = new CommentDatafetcher(service);
    when(service.findByArticleIdWithCursor(eq(article.getId()), any(), any()))
        .thenReturn(
            new CursorPager<>(Collections.singletonList(data), CursorPager.Direction.NEXT, false));
    DgsDataFetchingEnvironment dfe =
        GraphqlTestFixtures.dgsEnvironment(
            io.spring.graphql.types.Article.newBuilder().slug(article.getSlug()).build(),
            GraphqlTestFixtures.articleContext(
                article, GraphqlTestFixtures.articleData(article, user)));
    try (MockedStatic<SecurityUtil> security = org.mockito.Mockito.mockStatic(SecurityUtil.class)) {
      security.when(SecurityUtil::getCurrentUser).thenReturn(Optional.empty());
      assertEquals(
          1, fetcher.articleComments(1, null, null, null, dfe).getData().getEdges().size());
      assertThrows(
          IllegalArgumentException.class,
          () -> fetcher.articleComments(null, null, null, null, dfe));
    }
  }
}
