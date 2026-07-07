package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.CursorPager.Direction;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.rest.CommentServiceClient;
import java.util.Arrays;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentServiceClient commentServiceClient;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  private CommentData commentData(String id, String articleId) {
    return new CommentData(
        id,
        "content",
        articleId,
        new DateTime(),
        new DateTime(),
        new ProfileData("author-id", "aisensiy", "", "", false));
  }

  @Test
  public void should_read_comment_success() {
    CommentData data = commentData("comment-id", "123");
    when(commentServiceClient.findCommentDataById(eq("comment-id"), eq(user.getId())))
        .thenReturn(Optional.of(data));

    Optional<CommentData> optional = commentQueryService.findById("comment-id", user);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get().getProfileData().getUsername(), "aisensiy");
  }

  @Test
  public void should_read_comments_of_article() {
    when(commentServiceClient.findByArticleId(eq("article-id"), eq(user.getId())))
        .thenReturn(
            Arrays.asList(commentData("c1", "article-id"), commentData("c2", "article-id")));

    Assertions.assertEquals(2, commentQueryService.findByArticleId("article-id", user).size());
  }

  @Test
  public void should_page_comments_with_cursor() {
    when(commentServiceClient.findByArticleIdWithCursor(
            eq("article-id"), eq(user.getId()), any(), any(Integer.class), any(Boolean.class)))
        .thenReturn(
            Arrays.asList(commentData("c1", "article-id"), commentData("c2", "article-id")));

    CursorPager<CommentData> pager =
        commentQueryService.findByArticleIdWithCursor(
            "article-id", user, new CursorPageParameter<>(null, 20, Direction.NEXT));
    Assertions.assertEquals(2, pager.getData().size());
    Assertions.assertFalse(pager.hasNext());
  }
}
