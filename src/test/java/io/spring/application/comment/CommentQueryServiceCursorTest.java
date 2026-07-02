package io.spring.application.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceCursorTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "", "");
  }

  @Test
  void should_find_comment_by_id() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData commentData =
        new CommentData("comment-id", "body", "article-id", now, now, profile);
    when(commentReadService.findById(eq("comment-id"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("comment-id", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("not-exists"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("not-exists", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_by_article_id_with_cursor_next_direction() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_by_article_id_with_cursor_with_user_following() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_pager_when_no_comments() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  void should_handle_has_extra_with_next_direction() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile);
    CommentData c2 = new CommentData("c2", "body2", "article-id", now, now, profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_handle_has_extra_with_prev_direction_and_reverse() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile);
    CommentData c2 = new CommentData("c2", "body2", "article-id", now, now, profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.PREV);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasPrevious());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_by_article_id_with_null_user() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile);
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(Arrays.asList(c1));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
    verify(userRelationshipQueryService, never()).followingAuthors(any(), any());
  }

  @Test
  void should_find_by_article_id_with_empty_comments() {
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_user_and_following() {
    DateTime now = new DateTime();
    ProfileData profile1 = new ProfileData("author1-id", "author1", "bio", "img", false);
    ProfileData profile2 = new ProfileData("author2-id", "author2", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile1);
    CommentData c2 = new CommentData("c2", "body2", "article-id", now, now, profile2);
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(Arrays.asList(c1, c2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("author1-id")));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_cursor_user_not_following_any() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, profile);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1));
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertFalse(result.getData().get(0).getProfileData().isFollowing());
  }
}
