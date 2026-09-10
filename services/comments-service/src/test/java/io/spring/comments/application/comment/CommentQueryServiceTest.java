package io.spring.comments.application.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.comments.application.CommentQueryService;
import io.spring.comments.application.CursorPageParameter;
import io.spring.comments.application.CursorPager;
import io.spring.comments.application.CursorPager.Direction;
import io.spring.comments.application.data.CommentData;
import io.spring.comments.core.comment.Comment;
import io.spring.comments.core.comment.CommentRepository;
import io.spring.comments.core.user.CurrentUser;
import io.spring.comments.infrastructure.DbTestBase;
import io.spring.comments.infrastructure.monolith.MonolithClient;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import io.spring.comments.infrastructure.repository.MyBatisCommentRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({MyBatisCommentRepository.class, CommentQueryService.class})
public class CommentQueryServiceTest extends DbTestBase {
  @Autowired private CommentRepository commentRepository;

  @Autowired private CommentQueryService commentQueryService;

  @MockBean private MonolithClient monolithClient;

  private CurrentUser user;
  private CurrentUser user2;

  @BeforeEach
  public void setUp() {
    user = new CurrentUser(UUID.randomUUID().toString(), "aisensiy", "", "");
    user2 = new CurrentUser(UUID.randomUUID().toString(), "user2", "", "");
    when(monolithClient.findProfileById(eq(user.getId()), any()))
        .thenReturn(new ProfileDTO(user.getId(), user.getUsername(), "", "", false));
    when(monolithClient.findProfilesByIds(anyCollection(), any()))
        .thenReturn(
            Arrays.asList(
                new ProfileDTO(user.getId(), user.getUsername(), "", "", false),
                new ProfileDTO(user2.getId(), user2.getUsername(), "", "", true)));
  }

  @Test
  public void should_read_comment_success() {
    Comment comment = new Comment("content", user.getId(), "123");
    commentRepository.save(comment);

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
    Assertions.assertEquals(commentData.getCreatedAt(), commentData.getUpdatedAt());
  }

  @Test
  public void should_return_empty_when_comment_not_found() {
    Assertions.assertFalse(commentQueryService.findById("not-exist", user).isPresent());
  }

  @Test
  public void should_read_comments_of_article() {
    String articleId = UUID.randomUUID().toString();
    Comment comment1 = new Comment("content1", user.getId(), articleId);
    commentRepository.save(comment1);
    Comment comment2 = new Comment("content2", user2.getId(), articleId);
    commentRepository.save(comment2);

    List<CommentData> comments = commentQueryService.findByArticleId(articleId, user);
    Assertions.assertEquals(comments.size(), 2);
    Assertions.assertTrue(
        comments.stream()
            .anyMatch(
                commentData ->
                    commentData.getProfileData().getUsername().equals(user2.getUsername())
                        && commentData.getProfileData().isFollowing()));
  }

  @Test
  public void should_read_comments_with_cursor() {
    String articleId = UUID.randomUUID().toString();
    commentRepository.save(new Comment("content1", user.getId(), articleId));
    commentRepository.save(new Comment("content2", user2.getId(), articleId));

    CursorPager<CommentData> page =
        commentQueryService.findByArticleIdWithCursor(
            articleId, user, new CursorPageParameter<DateTime>(null, 1, Direction.NEXT));

    Assertions.assertEquals(1, page.getData().size());
    Assertions.assertTrue(page.hasNext());
    Assertions.assertFalse(page.hasPrevious());
    Assertions.assertNotNull(page.getStartCursor());
  }
}
