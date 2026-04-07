package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentsServiceClient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({CommentQueryService.class})
public class CommentQueryServiceTest extends DbTestBase {

  @MockBean private CommentsServiceClient commentsServiceClient;

  @MockBean private UserRelationshipQueryService userRelationshipQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData =
        new CommentData("comment-id", "content", "article-123", new DateTime(), new DateTime(), profileData);

    when(commentsServiceClient.findCommentDataById(eq("comment-id")))
        .thenReturn(Optional.of(commentData));
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    CommentQueryService commentQueryService =
        new CommentQueryService(commentsServiceClient, userRelationshipQueryService);

    Optional<CommentData> optional = commentQueryService.findById("comment-id", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData result = optional.get();
    Assertions.assertEquals(result.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    ProfileData profileData1 =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData1 =
        new CommentData("c1", "content1", "article-1", new DateTime(), new DateTime(), profileData1);

    User user2 = new User("user2@email.com", "user2", "123", "", "");
    ProfileData profileData2 =
        new ProfileData(user2.getId(), user2.getUsername(), user2.getBio(), user2.getImage(), false);
    CommentData commentData2 =
        new CommentData("c2", "content2", "article-1", new DateTime(), new DateTime(), profileData2);

    when(commentsServiceClient.findCommentDataByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(commentData1, commentData2));
    when(userRelationshipQueryService.followingAuthors(anyString(), eq(Arrays.asList(user.getId(), user2.getId()))))
        .thenReturn(java.util.Collections.singleton(user2.getId()));

    CommentQueryService commentQueryService =
        new CommentQueryService(commentsServiceClient, userRelationshipQueryService);

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
