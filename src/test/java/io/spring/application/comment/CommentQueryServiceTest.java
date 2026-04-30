package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
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
import java.time.Instant;
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
    CommentResponse response = new CommentResponse();
    response.setId("comment-1");
    response.setBody("content");
    response.setUserId(user.getId());
    response.setArticleId("123");
    response.setCreatedAt(Instant.now().toString());
    response.setUpdatedAt(Instant.now().toString());

    when(commentServiceClient.findById(eq("comment-1"))).thenReturn(Optional.of(response));
    UserData userData =
        new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), "");
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

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
    response1.setCreatedAt(Instant.now().toString());
    response1.setUpdatedAt(Instant.now().toString());

    CommentResponse response2 = new CommentResponse();
    response2.setId("c2");
    response2.setBody("content2");
    response2.setUserId("user2-id");
    response2.setArticleId("article-1");
    response2.setCreatedAt(Instant.now().toString());
    response2.setUpdatedAt(Instant.now().toString());

    when(commentServiceClient.findByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(response1, response2));

    UserData userData1 =
        new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), "");
    UserData userData2 = new UserData("user2-id", "user2@email.com", "user2", "", "");
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData1);
    when(userReadService.findById(eq("user2-id"))).thenReturn(userData2);

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
