package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  private CommentData createCommentData(String id) {
    return new CommentData(
        id,
        "body",
        "article-id",
        new DateTime(),
        new DateTime(),
        new ProfileData("author-id", "author", "bio", "image", false));
  }

  @Test
  public void should_find_by_id() {
    CommentData commentData = createCommentData("comment-1");
    when(commentReadService.findById(eq("comment-1"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("comment-1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("missing"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("missing", user);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_by_article_id_with_user() {
    CommentData commentData = createCommentData("comment-1");
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertFalse(result.isEmpty());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_by_article_id_without_user() {
    CommentData commentData = createCommentData("comment-1");
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertFalse(result.isEmpty());
  }

  @Test
  public void should_find_by_article_id_empty() {
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_by_article_id_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_by_article_id_with_cursor_with_user() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CommentData commentData = createCommentData("comment-1");
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertFalse(result.getData().isEmpty());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_by_article_id_with_cursor_without_user() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CommentData commentData = createCommentData("comment-1");
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  public void should_find_by_article_id_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    CommentData commentData = createCommentData("comment-1");
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertFalse(result.getData().isEmpty());
  }
}
