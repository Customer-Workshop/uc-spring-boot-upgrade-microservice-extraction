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
import io.spring.infrastructure.service.client.CommentServiceClient;
import io.spring.infrastructure.service.client.CommentServiceResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
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
  @Mock private UserRelationshipQueryService userRelationshipQueryService;
  @Mock private UserReadService userReadService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    CommentServiceResponse response = new CommentServiceResponse();
    response.setId("comment-id-1");
    response.setBody("content");
    response.setArticleId("article-123");
    response.setUserId(user.getId());
    response.setCreatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(new DateTime()));
    response.setUpdatedAt(response.getCreatedAt());

    when(commentServiceClient.findCommentById(eq("comment-id-1")))
        .thenReturn(Optional.of(response));

    UserData userData = new UserData(user.getId(), user.getEmail(), user.getUsername(), "", "");
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData);

    Optional<CommentData> optional = commentQueryService.findById("comment-id-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    CommentServiceResponse response1 = new CommentServiceResponse();
    response1.setId("comment-1");
    response1.setBody("content1");
    response1.setArticleId("article-1");
    response1.setUserId(user.getId());
    response1.setCreatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(new DateTime()));
    response1.setUpdatedAt(response1.getCreatedAt());

    CommentServiceResponse response2 = new CommentServiceResponse();
    response2.setId("comment-2");
    response2.setBody("content2");
    response2.setArticleId("article-1");
    response2.setUserId("user-2");
    response2.setCreatedAt(ISODateTimeFormat.dateTime().withZoneUTC().print(new DateTime()));
    response2.setUpdatedAt(response2.getCreatedAt());

    when(commentServiceClient.findCommentsByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(response1, response2));

    UserData userData1 = new UserData(user.getId(), user.getEmail(), user.getUsername(), "", "");
    when(userReadService.findById(eq(user.getId()))).thenReturn(userData1);
    UserData userData2 = new UserData("user-2", "user2@email.com", "user2", "", "");
    when(userReadService.findById(eq("user-2"))).thenReturn(userData2);

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
