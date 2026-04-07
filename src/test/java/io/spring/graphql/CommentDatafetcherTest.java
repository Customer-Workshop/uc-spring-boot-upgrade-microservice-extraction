package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.joda.time.DateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

class CommentDatafetcherTest {

  private CommentDatafetcher commentDatafetcher;
  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  void setUp() {
    commentQueryService = mock(CommentQueryService.class);
    commentDatafetcher = new CommentDatafetcher(commentQueryService);

    user = new User("test@example.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private CommentData createCommentData(String id) {
    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "author", "bio", "img", false);
    return new CommentData(id, "comment body", "article-id", now, now, profile);
  }

  @Test
  void should_get_comment_from_payload() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);
    CommentData commentData = createCommentData("c1");
    when(dfe.getLocalContext()).thenReturn(commentData);

    DataFetcherResult<Comment> result = commentDatafetcher.getComment(dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals("c1", result.getData().getId());
    assertEquals("comment body", result.getData().getBody());
  }

  @Test
  void should_get_article_comments_with_first() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-article").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "author", "bio", "img", false);
    ArticleData articleData = new ArticleData(
        "art-id", "test-article", "Title", "desc", "body", false, 0, now, now,
        Collections.emptyList(), profile);

    Map<String, ArticleData> localCtx = new HashMap<>();
    localCtx.put("test-article", articleData);
    when(dfe.getLocalContext()).thenReturn(localCtx);

    CommentData commentData = createCommentData("c1");
    CursorPager<CommentData> pager = new CursorPager<>(
        Arrays.asList(commentData), Direction.NEXT, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("art-id"), any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(10, null, null, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(1, result.getData().getEdges().size());
  }

  @Test
  void should_get_article_comments_with_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    Article article = Article.newBuilder().slug("test-article").build();
    when(dfe.getSource()).thenReturn(article);

    DateTime now = DateTime.now();
    ProfileData profile = new ProfileData("userId", "author", "bio", "img", false);
    ArticleData articleData = new ArticleData(
        "art-id", "test-article", "Title", "desc", "body", false, 0, now, now,
        Collections.emptyList(), profile);

    Map<String, ArticleData> localCtx = new HashMap<>();
    localCtx.put("test-article", articleData);
    when(dfe.getLocalContext()).thenReturn(localCtx);

    CursorPager<CommentData> pager = new CursorPager<>(
        Collections.emptyList(), Direction.PREV, false);
    when(commentQueryService.findByArticleIdWithCursor(eq("art-id"), any(), any(CursorPageParameter.class)))
        .thenReturn(pager);

    DataFetcherResult<CommentsConnection> result =
        commentDatafetcher.articleComments(null, null, 5, null, dfe);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void should_throw_when_article_comments_no_first_or_last() {
    DgsDataFetchingEnvironment dfe = mock(DgsDataFetchingEnvironment.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> commentDatafetcher.articleComments(null, null, null, null, dfe));
  }
}
