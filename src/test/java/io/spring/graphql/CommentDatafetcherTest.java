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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;
  @Mock private DgsDataFetchingEnvironment dfe;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;
  private ProfileData profileData;
  private CommentData commentData;
  private ArticleData articleData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
    commentData =
        new CommentData(
            "comment-id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    articleData =
        new ArticleData(
            "article-id",
            "test-slug",
            "Test Title",
            "desc",
            "body",
            false,
            0,
            new DateTime(),
            new DateTime(),
            Arrays.asList("java"),
            profileData);
    SecurityContextHolder.getContext()
        .setAuthentication(new TestingAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_get_comment() {
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertEquals("comment-id", result.getData().getId());
    assertEquals("body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_last() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);

    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_throw_when_comments_missing_first_and_last() {
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  void should_get_article_comments_empty() {
    Article article = Article.newBuilder().slug("test-slug").build();
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);
    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }
}
