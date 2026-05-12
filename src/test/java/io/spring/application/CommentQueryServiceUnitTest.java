package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

public class CommentQueryServiceUnitTest {

  private CommentReadService commentReadService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentReadService = mock(CommentReadService.class);
    userRelationshipQueryService = mock(UserRelationshipQueryService.class);
    commentQueryService =
        new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "pass", "", "");
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);
    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void should_return_comment_with_following_info() {
    ProfileData profile = new ProfileData("author-id", "author", "bio", "img", false);
    CommentData commentData =
        new CommentData("c1", "body", "a1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findById("c1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("c1", user);
    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_comments_for_article() {
    when(commentReadService.findByArticleId("a1")).thenReturn(Collections.emptyList());
    List<CommentData> result = commentQueryService.findByArticleId("a1", user);
    assertTrue(result.isEmpty());
  }

  @Test
  public void should_return_comments_with_following_info() {
    ProfileData profile1 = new ProfileData("author1", "auth1", "bio", "img", false);
    ProfileData profile2 = new ProfileData("author2", "auth2", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile1);
    CommentData c2 = new CommentData("c2", "body2", "a1", new DateTime(), new DateTime(), profile2);
    when(commentReadService.findByArticleId("a1")).thenReturn(Arrays.asList(c1, c2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Collections.singletonList("author1")));

    List<CommentData> result = commentQueryService.findByArticleId("a1", user);
    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  public void should_return_comments_without_following_info_when_user_null() {
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findByArticleId("a1")).thenReturn(Collections.singletonList(c1));

    List<CommentData> result = commentQueryService.findByArticleId("a1", null);
    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_by_article_id_with_cursor_empty() {
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>());

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_by_article_id_with_cursor_with_user() {
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Collections.singletonList(c1)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Collections.singletonList("author1")));

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_by_article_id_with_cursor_null_user() {
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Collections.singletonList(c1)));

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void should_find_by_article_id_with_cursor_has_extra() {
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    CommentData c2 = new CommentData("c2", "body2", "a1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);
    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }

  @Test
  public void should_find_by_article_id_with_cursor_prev_direction() {
    ProfileData profile = new ProfileData("author1", "auth1", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "a1", new DateTime(), new DateTime(), profile);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Collections.singletonList(c1)));

    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);
    assertEquals(1, result.getData().size());
  }
}
