package io.spring.application.comment;

import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.UserData;
import io.spring.core.comment.Comment;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

  @Mock private UserReadService userReadService;

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    Comment comment = new Comment("content", user.getId(), "123");

    when(commentServiceClient.findByCommentId(comment.getId()))
        .thenReturn(Optional.of(comment));
    when(userReadService.findById(user.getId()))
        .thenReturn(
            new UserData(
                user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage()));

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    String articleId = "article-123";
    Comment comment1 = new Comment("content1", user.getId(), articleId);
    User user2 = new User("user2@email.com", "user2", "123", "", "");
    Comment comment2 = new Comment("content2", user2.getId(), articleId);

    when(commentServiceClient.findByArticleId(articleId))
        .thenReturn(Arrays.asList(comment1, comment2));
    when(userReadService.findById(user.getId()))
        .thenReturn(
            new UserData(
                user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage()));
    when(userReadService.findById(user2.getId()))
        .thenReturn(
            new UserData(
                user2.getId(),
                user2.getEmail(),
                user2.getUsername(),
                user2.getBio(),
                user2.getImage()));

    List<CommentData> comments = commentQueryService.findByArticleId(articleId, user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
