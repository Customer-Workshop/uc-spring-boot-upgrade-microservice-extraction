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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.joda.time.DateTime;
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

  @Test
  public void should_find_comment_by_id() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData commentData =
        new CommentData("commentId", "comment body", "articleId", now, now, profile);

    when(commentReadService.findById(eq("commentId"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("authorId")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("commentId", user);

    assertTrue(result.isPresent());
    assertEquals("comment body", result.get().getBody());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    when(commentReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_comments_by_article_id_with_user() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    DateTime now = new DateTime();
    ProfileData profile1 = new ProfileData("author1", "authorName1", "bio", "img", false);
    ProfileData profile2 = new ProfileData("author2", "authorName2", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "articleId", now, now, profile1);
    CommentData c2 = new CommentData("c2", "body2", "articleId", now, now, profile2);

    when(commentReadService.findByArticleId(eq("articleId"))).thenReturn(Arrays.asList(c1, c2));
    Set<String> followingSet = new HashSet<>(Arrays.asList("author1"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(followingSet);

    List<CommentData> result = commentQueryService.findByArticleId("articleId", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_id_with_null_user() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData c = new CommentData("c1", "body", "articleId", now, now, profile);

    when(commentReadService.findByArticleId(eq("articleId"))).thenReturn(Arrays.asList(c));

    List<CommentData> result = commentQueryService.findByArticleId("articleId", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_comments_for_article() {
    when(commentReadService.findByArticleId(eq("articleId"))).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("articleId", null);

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_comments_by_article_with_cursor_empty() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("articleId"), any()))
        .thenReturn(new ArrayList<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("articleId", user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
  }

  @Test
  public void should_find_comments_by_article_with_cursor_with_data() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData c = new CommentData("c1", "body", "articleId", now, now, profile);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("articleId"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c)));
    Set<String> following = new HashSet<>(Arrays.asList("authorId"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("articleId", user, page);

    assertNotNull(result);
    assertFalse(result.getData().isEmpty());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_with_cursor_null_user() {
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData c = new CommentData("c1", "body", "articleId", now, now, profile);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("articleId"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("articleId", null, page);

    assertNotNull(result);
    assertFalse(result.getData().isEmpty());
  }

  @Test
  public void should_handle_extra_items_in_cursor_paging_next() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "articleId", now, now, profile);
    CommentData c2 = new CommentData("c2", "body2", "articleId", now.minusMinutes(1), now, profile);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("articleId"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("articleId", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasNext());
  }

  @Test
  public void should_handle_extra_items_in_cursor_paging_prev() {
    User user = new User("test@test.com", "testuser", "pass", "", "");
    DateTime now = new DateTime();
    ProfileData profile = new ProfileData("authorId", "author", "bio", "img", false);
    CommentData c1 = new CommentData("c1", "body1", "articleId", now, now, profile);
    CommentData c2 = new CommentData("c2", "body2", "articleId", now.minusMinutes(1), now, profile);

    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 1, CursorPager.Direction.PREV);

    when(commentReadService.findByArticleIdWithCursor(eq("articleId"), any()))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("articleId", user, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.hasPrevious());
  }
}
