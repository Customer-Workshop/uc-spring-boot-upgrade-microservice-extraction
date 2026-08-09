package io.spring.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.CommentReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

class CommentQueryServiceUnitTest {
  private final CommentReadService comments = mock(CommentReadService.class);
  private final UserRelationshipQueryService relationships = mock(UserRelationshipQueryService.class);
  private final CommentQueryService service = new CommentQueryService(comments, relationships);
  private final User user = new User("u@test.com", "user", "p", "", "");
  private final CommentData comment =
      new CommentData(
          "comment",
          "body",
          "article",
          new DateTime(),
          new DateTime(),
          new ProfileData("author", "author", "", "", false));

  @Test
  void findsCommentAndSetsFollowingStatus() {
    when(comments.findById("comment")).thenReturn(comment);
    when(relationships.isUserFollowing(user.getId(), "author")).thenReturn(true);
    assertTrue(service.findById("comment", user).orElseThrow().getProfileData().isFollowing());
    when(comments.findById("missing")).thenReturn(null);
    assertFalse(service.findById("missing", user).isPresent());
  }

  @Test
  void findsCommentsWithAndWithoutAuthenticatedUser() {
    CommentData second =
        new CommentData(
            "second", "body", "article", new DateTime(), new DateTime(),
            new ProfileData("other", "other", "", "", false));
    when(comments.findByArticleId("article")).thenReturn(Arrays.asList(comment, second));
    when(relationships.followingAuthors(user.getId(), Arrays.asList("author", "other")))
        .thenReturn(Collections.singleton("author"));
    assertEquals(2, service.findByArticleId("article", user).size());
    assertTrue(comment.getProfileData().isFollowing());
    assertFalse(second.getProfileData().isFollowing());
    when(comments.findByArticleId("empty")).thenReturn(Collections.emptyList());
    assertTrue(service.findByArticleId("empty", null).isEmpty());
    verify(relationships).followingAuthors(any(), any());
  }

  @Test
  void findsCursorPagesAndHandlesEmptyPreviousAndExtraRows() {
    CommentData second =
        new CommentData(
            "second", "body", "article", new DateTime().minusDays(1), new DateTime(),
            new ProfileData("other", "other", "", "", false));
    when(comments.findByArticleIdWithCursor(any(), any()))
        .thenAnswer(invocation -> new ArrayList<>(Arrays.asList(comment, second, comment)));
    when(relationships.followingAuthors(any(), any())).thenReturn(Set.of("author"));
    CursorPager<CommentData> next =
        service.findByArticleIdWithCursor(
            "article", user, new CursorPageParameter<>(null, 2, CursorPager.Direction.NEXT));
    assertEquals(2, next.getData().size());
    assertTrue(next.hasNext());
    CursorPager<CommentData> previous =
        service.findByArticleIdWithCursor(
            "article", user, new CursorPageParameter<>(null, 2, CursorPager.Direction.PREV));
    assertTrue(previous.hasPrevious());
    when(comments.findByArticleIdWithCursor(any(), any())).thenReturn(Collections.emptyList());
    CursorPager<CommentData> empty =
        service.findByArticleIdWithCursor(
            "article", null, new CursorPageParameter<>(null, 2, CursorPager.Direction.NEXT));
    assertTrue(empty.getData().isEmpty());
  }
}
