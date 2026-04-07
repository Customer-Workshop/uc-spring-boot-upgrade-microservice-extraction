package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

/**
 * Tests for CommentQueryService. Now that comments are served by an external microservice, these
 * tests verify the query service correctly delegates to the HTTP client and enriches responses with
 * user profile data.
 */
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
    Comment comment = new Comment("content", user.getId(), "123");
    when(commentServiceClient.findByIdOnly(eq(comment.getId())))
        .thenReturn(Optional.of(comment));
    when(userReadService.findById(eq(user.getId())))
        .thenReturn(new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage()));
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    Optional<CommentData> optional = commentQueryService.findById(comment.getId(), user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    Comment comment1 = new Comment("content1", user.getId(), "article-1");
    Comment comment2 = new Comment("content2", user.getId(), "article-1");
    when(commentServiceClient.findByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(comment1, comment2));
    when(userReadService.findById(eq(user.getId())))
        .thenReturn(new UserData(user.getId(), user.getEmail(), user.getUsername(), user.getBio(), user.getImage()));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
