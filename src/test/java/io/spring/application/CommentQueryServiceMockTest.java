package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

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
public class CommentQueryServiceMockTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "123", "", "");
  }

  @Test
  public void should_find_comment_by_id() {
    DateTime now = new DateTime();
    ProfileData profileData = new ProfileData("authorId", "author", "bio", "image", false);
    CommentData commentData = new CommentData("cid", "body", "articleId", now, now, profileData);
    when(commentReadService.findById(eq("cid"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("authorId")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("cid", user);
    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("nonexistent"))).thenReturn(null);
    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);
    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_comments_by_article_id_with_user() {
    DateTime now = new DateTime();
    ProfileData profileData = new ProfileData("authorId", "author", "bio", "image", false);
    CommentData commentData = new CommentData("cid", "body", "articleId", now, now, profileData);
    when(commentReadService.findByArticleId(eq("articleId")))
        .thenReturn(Arrays.asList(commentData));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("authorId")));

    List<CommentData> result = commentQueryService.findByArticleId("articleId", user);
    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_id_without_user() {
    DateTime now = new DateTime();
    ProfileData profileData = new ProfileData("authorId", "author", "bio", "image", false);
    CommentData commentData = new CommentData("cid", "body", "articleId", now, now, profileData);
    when(commentReadService.findByArticleId(eq("articleId")))
        .thenReturn(Arrays.asList(commentData));

    List<CommentData> result = commentQueryService.findByArticleId("articleId", null);
    assertEquals(1, result.size());
  }

  @Test
  public void should_return_empty_list_when_no_comments() {
    when(commentReadService.findByArticleId(eq("articleId"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("articleId", user);
    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_comments_with_cursor_first_page() {
    DateTime now = new DateTime();
    ProfileData profileData = new ProfileData("authorId", "author", "bio", "image", false);
    CommentData cd = new CommentData("cid", "body", "articleId", now, now, profileData);
    when(commentReadService.findByArticleIdWithCursor(any(), any())).thenReturn(Arrays.asList(cd));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Arrays.asList("authorId")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "articleId", user, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  public void should_return_empty_cursor_pager_when_no_comments() {
    when(commentReadService.findByArticleIdWithCursor(any(), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "articleId", user, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_find_comments_with_cursor_without_user() {
    DateTime now = new DateTime();
    ProfileData profileData = new ProfileData("authorId", "author", "bio", "image", false);
    CommentData cd = new CommentData("cid", "body", "articleId", now, now, profileData);
    when(commentReadService.findByArticleIdWithCursor(any(), any())).thenReturn(Arrays.asList(cd));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "articleId", null, new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT));
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  public void should_find_comments_with_cursor_prev_direction() {
    DateTime now = new DateTime();
    ProfileData profileData = new ProfileData("authorId", "author", "bio", "image", false);
    CommentData cd = new CommentData("cid", "body", "articleId", now, now, profileData);
    when(commentReadService.findByArticleIdWithCursor(any(), any())).thenReturn(Arrays.asList(cd));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor(
            "articleId", null, new CursorPageParameter<>(null, 10, CursorPager.Direction.PREV));
    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }
}
