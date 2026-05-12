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
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "password", "bio", "image");
    profileData = new ProfileData("author-id", "author", "bio", "image", false);
    DateTime now = new DateTime();
    commentData = new CommentData("comment-id", "body", "article-id", now, now, profileData);
  }

  @Test
  public void should_find_comment_by_id() {
    when(commentReadService.findById(eq("comment-id"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("comment-id", user);
    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("not-found"))).thenReturn(null);
    Optional<CommentData> result = commentQueryService.findById("not-found", user);
    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_comments_by_article_id() {
    List<CommentData> comments = Arrays.asList(commentData);
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(comments);
    Set<String> followingIds = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(followingIds);

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_id_without_user() {
    List<CommentData> comments = Arrays.asList(commentData);
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(comments);

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);
    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_id_empty() {
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(new ArrayList<>());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);
    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_comments_with_cursor_next() {
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  public void should_find_comments_with_cursor_prev() {
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.PREV);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void should_find_comments_with_cursor_empty() {
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(new ArrayList<>());

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_comments_with_cursor_has_extra() {
    DateTime now = new DateTime();
    ProfileData p1 = new ProfileData("a1", "author1", "bio", "img", false);
    ProfileData p2 = new ProfileData("a2", "author2", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, p1);
    CommentData c2 = new CommentData("c2", "body2", "article-id", now.plusSeconds(1), now.plusSeconds(1), p2);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  public void should_find_comments_with_cursor_has_extra_prev() {
    DateTime now = new DateTime();
    ProfileData p1 = new ProfileData("a1", "author1", "bio", "img", false);
    ProfileData p2 = new ProfileData("a2", "author2", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "article-id", now, now, p1);
    CommentData c2 = new CommentData("c2", "body2", "article-id", now.plusSeconds(1), now.plusSeconds(1), p2);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(c1, c2));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, Direction.PREV);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasPrevious());
  }

  @Test
  public void should_find_comments_with_cursor_and_user_following() {
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), any()))
        .thenReturn(comments);
    Set<String> followingIds = new HashSet<>(Arrays.asList("author-id"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(followingIds);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);
    assertNotNull(result);
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }
}
