package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.Set;
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
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@example.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData("author-id", "author", "bio", "image", false);
  }

  @Test
  void should_find_comment_by_id() {
    DateTime now = new DateTime();
    CommentData commentData = new CommentData("c1", "body", "a1", now, now, profileData);
    when(commentReadService.findById("c1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author-id")).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("c1")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_by_article_id_with_following() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    ProfileData pd2 = new ProfileData("author2-id", "author2", "", "", false);
    CommentData cd2 = new CommentData("c2", "body2", "a1", now, now, pd2);

    when(commentReadService.findByArticleId("a1")).thenReturn(Arrays.asList(cd1, cd2));
    Set<String> following = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    List<CommentData> result = commentQueryService.findByArticleId("a1", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_without_user() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    when(commentReadService.findByArticleId("a1")).thenReturn(Arrays.asList(cd1));

    List<CommentData> result = commentQueryService.findByArticleId("a1", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_list_for_article_without_comments() {
    when(commentReadService.findByArticleId("a1")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("a1", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_by_article_id_with_cursor_next() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    CommentData cd2 = new CommentData("c2", "body2", "a1", now, now, profileData);
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(cd1, cd2)));
    Set<String> following = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_find_comments_by_article_id_with_cursor_prev() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    CommentData cd2 = new CommentData("c2", "body2", "a1", now, now, profileData);
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, CursorPager.Direction.PREV);

    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(cd1, cd2)));
    Set<String> following = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_comments_with_cursor_empty() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_comments_with_cursor_no_user() {
    DateTime now = new DateTime();
    CommentData cd1 = new CommentData("c1", "body1", "a1", now, now, profileData);
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("a1"), any()))
        .thenReturn(Arrays.asList(cd1));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("a1", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.getData().get(0).getProfileData().isFollowing());
  }
}
