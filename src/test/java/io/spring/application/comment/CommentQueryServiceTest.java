package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.UserData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserReadService;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentResponse;
import io.spring.infrastructure.service.CommentServiceClient;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    response.setArticleId("article-1");
    response.setCreatedAt(Instant.now().toString());

    when(commentServiceClient.findResponseById("comment-1")).thenReturn(Optional.of(response));
    when(userReadService.findById(user.getId()))
        .thenReturn(new UserData(user.getId(), user.getEmail(), user.getUsername(), "", ""));

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    CommentResponse r1 = new CommentResponse();
    r1.setId("c1");
    r1.setBody("content1");
    r1.setUserId(user.getId());
    r1.setArticleId("article-1");
    r1.setCreatedAt(Instant.now().toString());

    CommentResponse r2 = new CommentResponse();
    r2.setId("c2");
    r2.setBody("content2");
    r2.setUserId("user-2");
    r2.setArticleId("article-1");
    r2.setCreatedAt(Instant.now().toString());

    when(commentServiceClient.findByArticleId("article-1")).thenReturn(Arrays.asList(r1, r2));
    when(userReadService.findById(anyString()))
        .thenReturn(new UserData(user.getId(), user.getEmail(), user.getUsername(), "", ""));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
