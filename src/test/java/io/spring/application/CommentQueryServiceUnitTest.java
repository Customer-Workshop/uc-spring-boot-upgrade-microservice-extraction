package io.spring.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
public class CommentQueryServiceUnitTest {

  @Mock private CommentReadService commentReadService;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;
  private CommentData commentData;
  private ProfileData profileData;

  @BeforeEach
  void setUp() {
    user = new User("test@test.com", "testuser", "pass", "bio", "image");
    profileData = new ProfileData("authorId", "author", "bio", "image", false);
    commentData =
        new CommentData(
            "c1", "comment body", "article1", new DateTime(), new DateTime(), profileData);
  }

  @Test
  void should_find_by_id_and_set_following() {
    when(commentReadService.findById("c1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "authorId")).thenReturn(true);

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("nonexistent")).thenReturn(null);

    Optional<CommentData> result = commentQueryService.findById("nonexistent", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_user() {
    List<CommentData> comments = Arrays.asList(commentData);
    when(commentReadService.findByArticleId("article1")).thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("authorId")));

    List<CommentData> result = commentQueryService.findByArticleId("article1", user);

    assertEquals(1, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_with_null_user() {
    List<CommentData> comments = Arrays.asList(commentData);
    when(commentReadService.findByArticleId("article1")).thenReturn(comments);

    List<CommentData> result = commentQueryService.findByArticleId("article1", null);

    assertEquals(1, result.size());
    assertFalse(result.get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_with_empty_comments() {
    when(commentReadService.findByArticleId("article1")).thenReturn(Collections.emptyList());

    List<CommentData> result = commentQueryService.findByArticleId("article1", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_by_article_id_with_cursor_empty_results() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(Collections.emptyList());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    assertTrue(result.getData().isEmpty());
    assertFalse(result.hasNext());
    assertFalse(result.hasPrevious());
  }

  @Test
  void should_find_by_article_id_with_cursor_next_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any())).thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(new HashSet<>(Arrays.asList("authorId")));

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    assertFalse(result.getData().isEmpty());
    assertTrue(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  void should_find_by_article_id_with_cursor_prev_direction() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.PREV);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any())).thenReturn(comments);
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), anyList()))
        .thenReturn(Collections.emptySet());

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", user, page);

    assertFalse(result.getData().isEmpty());
  }

  @Test
  void should_find_by_article_id_with_cursor_null_user() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData));
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any())).thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", null, page);

    assertFalse(result.getData().isEmpty());
    assertFalse(result.getData().get(0).getProfileData().isFollowing());
  }

  @Test
  void should_detect_extra_page_in_cursor_query() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 1, Direction.NEXT);
    ProfileData profile2 = new ProfileData("author2", "author2", "bio", "img", false);
    CommentData comment2 =
        new CommentData("c2", "body2", "article1", new DateTime(), new DateTime(), profile2);
    List<CommentData> comments = new ArrayList<>(Arrays.asList(commentData, comment2));
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any())).thenReturn(comments);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article1", null, page);

    assertTrue(result.hasNext());
    assertEquals(1, result.getData().size());
  }
}
