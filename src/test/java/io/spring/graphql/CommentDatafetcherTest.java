package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommentDatafetcherTest {

  @Mock private CommentQueryService commentQueryService;

  @InjectMocks private CommentDatafetcher commentDatafetcher;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_get_comment() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    CommentData commentData =
        new CommentData("commentId", "comment body", "articleId", now, now, profile);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    doReturn(commentData).when(dfe).getLocalContext();

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("commentId", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  public void should_get_article_comments_with_first() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "articleId", now, now, profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(new ArrayList<>(Arrays.asList(c1)), Direction.NEXT, false);

    when(commentQueryService.findByArticleIdWithCursor(eq("articleId"), any(), any()))
        .thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData =
        new ArticleData(
            "articleId", "test-slug", "title", "desc", "body", false, 0, now, now, null, profile);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    doReturn(map).when(dfe).getLocalContext();

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  public void should_get_article_comments_with_last() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData(user.getId(), "testuser", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "articleId", now, now, profile);
    CursorPager<CommentData> pager =
        new CursorPager<>(new ArrayList<>(Arrays.asList(c1)), Direction.PREV, false);

    when(commentQueryService.findByArticleIdWithCursor(eq("articleId"), any(), any()))
        .thenReturn(pager);

    Article article = Article.newBuilder().slug("test-slug").build();
    ArticleData articleData =
        new ArticleData(
            "articleId", "test-slug", "title", "desc", "body", false, 0, now, now, null, profile);
    Map<String, ArticleData> map = new HashMap<>();
    map.put("test-slug", articleData);

    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    when(dfe.getSource()).thenReturn(article);
    doReturn(map).when(dfe).getLocalContext();

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 10, null, dfe);

    assertNotNull(result);
  }

  @Test
  public void should_throw_when_first_and_last_are_null() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
