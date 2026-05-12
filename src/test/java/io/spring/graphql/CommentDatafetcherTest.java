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
    DateTime now = new DateTime();
    commentData =
        new CommentData(
            "comment-id",
            "comment body",
            "article-id",
            now,
            now,
            new ProfileData(user.getId(), "testuser", "bio", "image", false));
    setAnonymousAuth();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setAnonymousAuth() {
    SecurityContextHolder.getContext()
        .setAuthentication(
            new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
  }

  private void authenticateUser(User u) {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null));
  }

  @Test
  public void should_get_comment_from_payload() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);
    assertNotNull(result);
    assertEquals("comment-id", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  public void should_get_article_comments_with_first() {
    authenticateUser(user);

    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), eq(user), any()))
        .thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData = new ArticleData();
    articleData.setId("article-id");
    articleData.setSlug("test-slug");
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
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(commentData), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData = new ArticleData();
    articleData.setId("article-id");
    articleData.setSlug("test-slug");
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);
    assertNotNull(result);
  }

  @Test
  public void should_throw_when_comments_missing_first_and_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }

  @Test
  public void should_get_article_comments_empty() {
    CursorPager<CommentData> pager =
        new CursorPager<>(new ArrayList<>(), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("article-id"), any(), any()))
        .thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData = new ArticleData();
    articleData.setId("article-id");
    articleData.setSlug("test-slug");
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    when(dfe.getLocalContext()).thenReturn(map);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);
    assertNotNull(result);
    assertEquals(0, result.getData().getEdges().size());
  }
}
