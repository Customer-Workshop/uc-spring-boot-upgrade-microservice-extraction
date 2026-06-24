package io.spring.application.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.client.CommentServiceClient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommentQueryServiceTest {

  private CommentServiceClient commentServiceClient;
  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentServiceClient = mock(CommentServiceClient.class);
    commentQueryService = new CommentQueryService(commentServiceClient);
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  private CommentData sampleComment(String id) {
    return new CommentData(
        id,
        "content",
        "article-1",
        new DateTime(),
        new DateTime(),
        new ProfileData("u1", "aisensiy", "", "", false));
  }

  @Test
  public void should_delegate_find_by_id_with_viewer() {
    CommentData data = sampleComment("c1");
    when(commentServiceClient.findCommentData(eq("c1"), eq(user.getId())))
        .thenReturn(Optional.of(data));

    Optional<CommentData> result = commentQueryService.findById("c1", user);

    assertTrue(result.isPresent());
    assertSame(data, result.get());
  }

  @Test
  public void should_pass_null_viewer_when_user_is_null() {
    when(commentServiceClient.findByArticleId(eq("article-1"), isNull()))
        .thenReturn(Arrays.asList(sampleComment("c1"), sampleComment("c2")));

    List<CommentData> result = commentQueryService.findByArticleId("article-1", null);

    assertEquals(2, result.size());
  }

  @Test
  public void should_delegate_cursor_query() {
    CursorPageParameter<DateTime> page = new CursorPageParameter<>(null, 10, Direction.NEXT);
    CursorPager<CommentData> pager =
        new CursorPager<>(Arrays.asList(sampleComment("c1")), Direction.NEXT, false);
    when(commentServiceClient.findByArticleIdWithCursor(
            eq("article-1"), eq(user.getId()), eq(page)))
        .thenReturn(pager);

    CursorPager<CommentData> result =
        commentQueryService.findByArticleIdWithCursor("article-1", user, page);

    assertEquals(1, result.getData().size());
  }
}
