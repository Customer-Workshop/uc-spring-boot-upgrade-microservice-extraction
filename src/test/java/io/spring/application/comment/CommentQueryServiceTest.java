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
  private UserData userData;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
    userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), "", "");
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

    when(commentServiceClient.getCommentById(eq("comment-1"))).thenReturn(Optional.of(response));
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
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
    response1.setId("comment-1");
    response1.setBody("content1");
    response1.setUserId(user.getId());
    response1.setArticleId("article-1");
    response1.setCreatedAt("2026-01-01T00:00:00.000Z");
    response1.setUpdatedAt("2026-01-01T00:00:00.000Z");

    CommentResponse response2 = new CommentResponse();
    response2.setId("comment-2");
    response2.setBody("content2");
    response2.setUserId("user-2");
    response2.setArticleId("article-1");
    response2.setCreatedAt("2026-01-02T00:00:00.000Z");
    response2.setUpdatedAt("2026-01-02T00:00:00.000Z");

    UserData userData2 = new UserData("user-2", "user2@email.com", "user2", "", "");

    when(commentServiceClient.getCommentsByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(response1, response2));
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(userReadService.findById(eq("user-2"))).thenReturn(userData2);

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
