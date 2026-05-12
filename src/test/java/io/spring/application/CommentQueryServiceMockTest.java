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
public class CommentQueryServiceMockTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    profileData = new ProfileData(user.getId(), "testuser", "bio", "image", false);
  }

  @Test
  void should_find_by_id_returns_empty_when_not_found() {
    when(commentReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_by_id_returns_comment_with_following_info() {
    CommentData commentData =
        new CommentData("id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findById(eq("id"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq(user.getId())))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("id", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_returns_empty_list() {
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(new ArrayList<>());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_null_user() {
    CommentData commentData =
        new CommentData("id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
  }

  @Test
  void should_find_by_article_id_with_cursor_empty_result() {
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", user, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));

    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_by_article_id_with_cursor_with_user() {
    CommentData commentData =
        new CommentData("id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData)));

    Set<String> following = new HashSet<>(Arrays.asList(user.getId()));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", user, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));

    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_with_cursor_null_user() {
    CommentData commentData =
        new CommentData("id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", null, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_by_article_id_with_cursor_prev_direction() {
    CommentData commentData =
        new CommentData("id", "body", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", null, new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV));

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_find_by_article_id_with_cursor_has_extra() {
    CommentData c1 =
        new CommentData("id1", "body1", "article-id", new DateTime(), new DateTime(), profileData);
    CommentData c2 =
        new CommentData("id2", "body2", "article-id", new DateTime(), new DateTime(), profileData);
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", null, new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT));

    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }
}
