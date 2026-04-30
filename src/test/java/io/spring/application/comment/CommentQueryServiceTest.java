package io.spring.application.comment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.*;
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

  private CommentQueryService service;
  private User user;

  @BeforeEach
  void setUp() {
    service = new CommentQueryService(commentReadService, userRelationshipQueryService);
    user = new User("test@test.com", "testuser", "password", "bio", "image");
  }

  @Test
  void should_find_comment_by_id() {
    CommentData commentData = createCommentData("c1", "author1");
    when(commentReadService.findById("c1")).thenReturn(commentData);
    when(userRelationshipQueryService.isUserFollowing(user.getId(), "author1")).thenReturn(true);

    Optional<CommentData> result = service.findById("c1", user);

    assertTrue(result.isPresent());
    assertTrue(result.get().getProfileData().isFollowing());
  }

  @Test
  void should_return_empty_when_comment_not_found() {
    when(commentReadService.findById("missing")).thenReturn(null);

    Optional<CommentData> result = service.findById("missing", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_by_article_id() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    when(commentReadService.findByArticleId("article1")).thenReturn(List.of(c1, c2));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(Set.of("author1"));

    List<CommentData> result = service.findByArticleId("article1", user);

    assertEquals(2, result.size());
    assertTrue(result.get(0).getProfileData().isFollowing());
    assertFalse(result.get(1).getProfileData().isFollowing());
  }

  @Test
  void should_find_comments_by_article_id_with_null_user() {
    CommentData c1 = createCommentData("c1", "author1");
    when(commentReadService.findByArticleId("article1")).thenReturn(List.of(c1));

    List<CommentData> result = service.findByArticleId("article1", null);

    assertEquals(1, result.size());
  }

  @Test
  void should_find_comments_by_article_id_empty_list() {
    when(commentReadService.findByArticleId("article1")).thenReturn(Collections.emptyList());

    List<CommentData> result = service.findByArticleId("article1", user);

    assertTrue(result.isEmpty());
  }

  @Test
  void should_find_comments_with_cursor_next() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(List.of(c1, c2)));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(Set.of("author1"));

    CursorPager<CommentData> result =
        service.findByArticleIdWithCursor(
            "article1", user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    assertNotNull(result);
    assertEquals(2, result.getData().size());
    assertFalse(result.hasNext());
  }

  @Test
  void should_find_comments_with_cursor_prev() {
    CommentData c1 = createCommentData("c1", "author1");
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(List.of(c1)));

    CursorPager<CommentData> result =
        service.findByArticleIdWithCursor(
            "article1", null, new CursorPageParameter<>(null, 10, Direction.PREV));

    assertNotNull(result);
    assertEquals(1, result.getData().size());
  }

  @Test
  void should_return_empty_pager_when_no_comments() {
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>());

    CursorPager<CommentData> result =
        service.findByArticleIdWithCursor(
            "article1", user, new CursorPageParameter<>(null, 10, Direction.NEXT));

    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
  }

  @Test
  void should_handle_has_extra_with_cursor() {
    CommentData c1 = createCommentData("c1", "author1");
    CommentData c2 = createCommentData("c2", "author2");
    CommentData extra = createCommentData("c3", "author3");
    when(commentReadService.findByArticleIdWithCursor(eq("article1"), any()))
        .thenReturn(new ArrayList<>(List.of(c1, c2, extra)));

    CursorPager<CommentData> result =
        service.findByArticleIdWithCursor(
            "article1", null, new CursorPageParameter<>(null, 2, Direction.NEXT));

    assertNotNull(result);
    assertEquals(2, result.getData().size());
    assertTrue(result.hasNext());
  }

  private CommentData createCommentData(String id, String authorId) {
    return new CommentData(
        id,
        "body",
        "article1",
        new DateTime(),
        new DateTime(),
        new ProfileData(authorId, "user-" + authorId, "bio", "image", false));
  }
}
