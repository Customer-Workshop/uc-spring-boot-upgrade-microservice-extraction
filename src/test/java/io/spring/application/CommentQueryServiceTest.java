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
  private CommentData commentData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    commentData =
        new CommentData(
            "comment-id",
            "body",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData(user.getId(), user.getUsername(), "", "", false));
  }

  @Test
  void should_find_comment_by_id() {
    when(commentReadService.findById(eq("comment-id"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(any(), any())).thenReturn(false);

    Optional<CommentData> result = commentQueryService.findById("comment-id", user);

    assertTrue(result.isPresent());
    assertEquals("comment-id", result.get().getId());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  void should_find_comments_by_article_id() {
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(commentData));
    Set<String> following = new HashSet<>();
    following.add(user.getId());
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_without_user() {
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_list_for_article_with_no_comments() {
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_by_article_id_with_cursor_first() {
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));
    Set<String> following = new HashSet<>();
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", user, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));

    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_comments_by_article_id_with_cursor_empty() {
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", user, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));

    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_find_comments_by_article_id_with_cursor_null_user() {
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(Arrays.asList(commentData));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", null, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));

    assertEquals(1, result.getData().size());
  }

  @Test
  void should_handle_has_extra_with_cursor() {
    CommentData comment2 =
        new CommentData(
            "comment-2",
            "body2",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData("other-id", "other", "", "", false));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData, comment2)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", null, new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT));

    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  void should_reverse_when_direction_is_prev() {
    CommentData comment2 =
        new CommentData(
            "comment-2",
            "body2",
            "article-id",
            new DateTime(),
            new DateTime(),
            new ProfileData("other-id", "other", "", "", false));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(commentData, comment2)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", null, new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV));

    assertEquals(2, result.getData().size());
    assertEquals("comment-2", result.getData().get(0).getId());
    assertEquals("comment-id", result.getData().get(1).getId());
  }
}
