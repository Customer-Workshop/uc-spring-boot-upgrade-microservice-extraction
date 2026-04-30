package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.any;
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
import java.util.Collections;
import java.util.HashSet;
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
    CommentResponse response =
        new CommentResponse(
            "comment-1",
            "content",
            user.getId(),
            "123",
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.000Z");

    when(commentServiceClient.getCommentById(eq("comment-1"))).thenReturn(Optional.of(response));
    when(userReadService.findById(eq(user.getId())))
        .thenReturn(new UserData(user.getId(), user.getEmail(), user.getUsername(), "", ""));
    when(userRelationshipQueryService.isUserFollowing(any(), any())).thenReturn(false);

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    User user2 = new User("user2@email.com", "user2", "123", "", "");

    CommentResponse response1 =
        new CommentResponse(
            "c1",
            "content1",
            user.getId(),
            "article-1",
            "2026-01-01T00:00:00.000Z",
            "2026-01-01T00:00:00.000Z");
    CommentResponse response2 =
        new CommentResponse(
            "c2",
            "content2",
            user2.getId(),
            "article-1",
            "2026-01-02T00:00:00.000Z",
            "2026-01-02T00:00:00.000Z");

    when(commentServiceClient.getCommentsByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(response1, response2));
    when(userReadService.findById(eq(user.getId())))
        .thenReturn(new UserData(user.getId(), user.getEmail(), user.getUsername(), "", ""));
    when(userReadService.findById(eq(user2.getId())))
        .thenReturn(new UserData(user2.getId(), user2.getEmail(), user2.getUsername(), "", ""));
    when(userRelationshipQueryService.followingAuthors(eq(user.getId()), any()))
        .thenReturn(new HashSet<>(Collections.singletonList(user2.getId())));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
