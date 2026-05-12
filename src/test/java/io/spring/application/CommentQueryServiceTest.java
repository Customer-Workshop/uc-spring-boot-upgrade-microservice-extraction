package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
public class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;
  private CommentData commentData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    DateTime now = new DateTime();
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    commentData =
        new CommentData("comment-id", "Nice article", "article-id", now, now, profileData);
  }

  @Test
  void should_find_comment_by_id() {
    when(commentReadService.findById("comment-id")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), user.getId()))
        .thenReturn(false);

    Optional<CommentData> result = commentQueryService.findById("comment-id", user);

    assertTrue(result.isPresent());
    assertEquals("comment-id", result.get().getId());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_comments_by_article_id() {
    when(commentReadService.findByArticleId("article-id")).thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList(user.getId())));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_without_user() {
    when(commentReadService.findByArticleId("article-id")).thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_comments_for_article() {
    when(commentReadService.findByArticleId("article-id")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_with_cursor_next() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_comments_with_cursor_prev() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertFalse(result.hasPrevious());
  }

  @Test
  void should_return_empty_cursor_pager_when_no_comments() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_comments_with_cursor_without_user() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_handle_has_extra_comments() {
    DateTime now = new DateTime();
    ProfileData profileData2 = new ProfileData("user2-id", "user2", "", "", false);
    CommentData comment2 = new CommentData("c2", "body2", "article-id", now, now, profileData2);
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData, comment2)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }
}
