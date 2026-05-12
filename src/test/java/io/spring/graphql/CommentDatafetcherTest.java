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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  private CommentDatafetcher commentDatafetcher;
  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "123", "bio", "image");
    commentDatafetcher = new CommentDatafetcher(commentQueryService);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void authenticateUser(User u) {
    TestingAuthenticationToken auth = new TestingAuthenticationToken(u, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  private void setAnonymous() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  @Test
  public void should_get_comment_from_payload() {
    DateTime now = new DateTime();
    CommentData commentData =
        new CommentData(
            "commentId",
            "comment body",
            "articleId",
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), "bio", "image", false));

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);
    assertNotNull(result);
    assertEquals("commentId", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
    assertNotNull(result.getData().getCreatedAt());
    assertNotNull(result.getData().getUpdatedAt());
  }

  @Test
  public void should_get_article_comments_with_first() {
    authenticateUser(user);
    DateTime now = new DateTime();
    CommentData cd =
        new CommentData(
            "c1",
            "body",
            "articleId",
            now,
            now,
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    CursorPager<CommentData> pager = new CursorPager<>(Arrays.asList(cd), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData =
        new ArticleData(
            "articleId",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_article_comments_with_last() {
    authenticateUser(user);
    CursorPager<CommentData> pager = new CursorPager<>(new ArrayList<>(), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(any(), any(), any())).thenReturn(pager);

    DateTime now = new DateTime();
    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData =
        new ArticleData(
            "articleId",
            "test-slug",
            "title",
            "desc",
            "body",
            false,
            0,
            now,
            now,
            new ArrayList<>(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);
    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }

  @Test
  public void should_throw_when_both_null_article_comments() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
