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
    ProfileData profileData = new ProfileData("author-id", "author", "bio", "img", false);
    commentData =
        new CommentData("c1", "comment body", "a1", new DateTime(), new DateTime(), profileData);
  }

  @Test
  void should_find_comment_by_id() {
    when(commentReadService.findById(eq("c1"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("missing"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("missing", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_comments_by_article_id_with_user() {
    when(commentReadService.findByArticleId(eq("a1"))).thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    List<CommentData> result = commentQueryService.findByArticleId("a1", user);

    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_without_user() {
    when(commentReadService.findByArticleId(eq("a1"))).thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("a1", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_empty_list() {
    when(commentReadService.findByArticleId(eq("a1"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("a1", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_with_cursor_empty() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_comments_with_cursor_with_user() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("author-id")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_with_cursor_without_user() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_comments_with_cursor_has_extra() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    ProfileData p2 = new ProfileData("a2", "author2", "", "", false);
    CommentData c2 = new CommentData("c2", "body2", "a1", new DateTime(), new DateTime(), p2);
    List<CommentData> list = new ArrayList<>(Arrays.asList(commentData, c2));
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any())).thenReturn(list);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);

    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_reverse_comments_for_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    ProfileData p2 = new ProfileData("a2", "author2", "", "", false);
    CommentData c2 = new CommentData("c2", "body2", "a1", new DateTime(), new DateTime(), p2);
    List<CommentData> list = new ArrayList<>(Arrays.asList(commentData, c2));
    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any())).thenReturn(list);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);

    assertEquals(2, result.getData().size());
    assertEquals("c2", result.getData().get(0).getId());
    assertEquals("c1", result.getData().get(1).getId());
  }
}
