package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService = new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "123", "bio", "image");
  }

  private CommentData createCommentData(String id, String authorId) {
    ProfileData profileData = new ProfileData(authorId, "author", "bio", "img", false);
    CommentData commentData = new CommentData();
    commentData.setId(id);
    commentData.setBody("body");
    commentData.setArticleId("articleId");
    commentData.setCreatedAt(new DateTime());
    commentData.setUpdatedAt(new DateTime());
    commentData.setProfileData(profileData);
    return commentData;
  }

  @Test
  public void should_find_comment_by_id() {
    CommentData commentData = createCommentData("comment-id", "author-id");
    when(commentReadService.findById(eq("comment-id"))).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq("author-id")))
        .thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("comment-id", user);

    assertTrue(result.isPresent());
    assertEquals("comment-id", result.get().getId());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById(eq("nonexistent"))).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertFalse(result.isPresent());
  }

  @Test
  public void should_find_comments_by_article_id() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    when(commentReadService.findByArticleId(eq("article-id"))).thenReturn(Arrays.asList(c1, c2));
    Set<String> following = new HashSet<>(Arrays.asList("author1"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(following);

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  public void should_find_comments_by_article_id_without_user() {
    CommentData c1 = createCommentData("c1", "author1");
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Arrays.asList(c1));

    List<CommentData> result = commentQueryService.findByArticleId("article-id", null);

    assertEquals(1, result.size());
    verify(userRelationshipQueryService, never()).followingAuthors(anyString(), anyList());
  }

  @Test
  public void should_return_empty_list_when_no_comments() {
    when(commentReadService.findByArticleId(eq("article-id")))
        .thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article-id", user);

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_find_comments_with_cursor_and_user() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList(c1, c2)));
    Set<String> following = new HashSet<>(Arrays.asList("author1"));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(following);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
  }

  @Test
  public void should_find_comments_with_cursor_no_user() {
    CommentData c1 = createCommentData("c1", "author1");
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), eq(page)))
        .thenReturn(new ArrayList<>(Arrays.asList(c1)));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(1, result.getData().size());
    verify(userRelationshipQueryService, never()).followingAuthors(anyString(), anyList());
  }

  @Test
  public void should_return_empty_cursor_pager_when_no_comments() {
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), eq(page)))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", user, page);

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  public void should_handle_has_extra_with_cursor_next() {
    List<CommentData> comments = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      comments.add(createCommentData("c" + i, "author" + i));
    }
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 2, CursorPager.Direction.NEXT);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), eq(page)))
        .thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
  }

  @Test
  public void should_handle_has_extra_with_cursor_prev() {
    List<CommentData> comments = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      comments.add(createCommentData("c" + i, "author" + i));
    }
    CursorPageParameter<DateTime> page =
        new CursorPageParameter<>(null, 2, CursorPager.Direction.PREV);

    when(commentReadService.findByArticleIdWithCursor(eq("article-id"), eq(page)))
        .thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-id", null, page);

    assertNotNull(result);
    assertEquals(2, result.getData().size());
  }
}
