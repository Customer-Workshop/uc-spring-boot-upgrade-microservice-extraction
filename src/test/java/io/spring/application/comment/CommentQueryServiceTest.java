package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CommentQueryServiceTest {

  @Mock private CommentServiceClient commentServiceClient;
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private UserReadService userReadService;

  private CommentQueryService commentQueryService;
  private User user;

  @BeforeEach
  public void setUp() {
    commentQueryService =
        new CommentQueryService(
            commentServiceClient, userRelationshipQueryService, userReadService);
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    CommentResponse response = new CommentResponse();
    response.setId("comment-1");
    response.setBody("content");
    response.setUserId(user.getId());
    response.setArticleId("123");
    response.setCreatedAt("2026-01-01T00:00:00.000Z");
    response.setUpdatedAt("2026-01-01T00:00:00.000Z");

    when(commentServiceClient.getComment(eq("comment-1"))).thenReturn(Optional.of(response));
    when(userReadService.findById(eq(user.getId())))
        .thenReturn(
            new UserData(
                user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage()));
    when(userRelationshipQueryService.isUserFollowing(eq(user.getId()), eq(user.getId())))
        .thenReturn(false);

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    CommentResponse response1 = new CommentResponse();
    response1.setId("c1");
    response1.setBody("content1");
    response1.setUserId(user.getId());
    response1.setArticleId("article-1");
    response1.setCreatedAt("2026-01-01T00:00:00.000Z");

    String user2Id = "user-2-id";
    CommentResponse response2 = new CommentResponse();
    response2.setId("c2");
    response2.setBody("content2");
    response2.setUserId(user2Id);
    response2.setArticleId("article-1");
    response2.setCreatedAt("2026-01-02T00:00:00.000Z");

    when(commentServiceClient.getCommentsByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(response1, response2));
    when(userReadService.findById(eq(user.getId())))
        .thenReturn(
            new UserData(
                user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage()));
    when(userReadService.findById(eq(user2Id)))
        .thenReturn(new UserData(user2Id, "user2@email.com", "user2", "", ""));
    when(userRelationshipQueryService.followingAuthors(
            eq(user.getId()), ArgumentMatchers.anyList()))
        .thenReturn(Set.of(user2Id));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
