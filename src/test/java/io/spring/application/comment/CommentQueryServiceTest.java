package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentDto;
import io.spring.infrastructure.service.HttpCommentReadService;
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
  @Mock private io.spring.infrastructure.mybatis.readservice.UserReadService userReadService;

  private CommentQueryService commentQueryService;
  private HttpCommentReadService httpCommentReadService;
  private User user;

  @BeforeEach
  public void setUp() {
    httpCommentReadService =
        new HttpCommentReadService(commentServiceClient, userReadService);
    commentQueryService =
        new CommentQueryService(httpCommentReadService, userRelationshipQueryService);
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    CommentDto dto =
        new CommentDto("comment-1", "content", user.getId(), "123", Instant.now(), Instant.now());
    when(commentServiceClient.findById("comment-1")).thenReturn(Optional.of(dto));

    io.spring.application.data.UserData userData =
        new io.spring.application.data.UserData(
            user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
    when(userReadService.findById(user.getId())).thenReturn(userData);
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(user.getUsername(), commentData.getProfileData().getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    String articleId = "article-1";
    CommentDto dto1 =
        new CommentDto(
            "c1", "content1", user.getId(), articleId, Instant.now(), Instant.now());
    CommentDto dto2 =
        new CommentDto("c2", "content2", "user2-id", articleId, Instant.now(), Instant.now());

    when(commentServiceClient.findByArticleId(articleId)).thenReturn(Arrays.asList(dto1, dto2));

    io.spring.application.data.UserData userData =
        new io.spring.application.data.UserData(
            user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage());
    when(userReadService.findById(user.getId())).thenReturn(userData);

    io.spring.application.data.UserData user2Data =
        new io.spring.application.data.UserData("user2-id", "u2@e.com", "user2", "", "");
    when(userReadService.findById("user2-id")).thenReturn(user2Data);

    List<CommentData> comments = commentQueryService.findByArticleId(articleId, user);
    Assertions.assertEquals(2, comments.size());
  }
}
