package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.user.FollowRelation;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.infrastructure.DbTestBase;
import io.spring.infrastructure.repository.MyBatisUserRepository;
import io.spring.infrastructure.service.comment.CommentServiceClient;
import io.spring.infrastructure.service.comment.CommentServiceClient.CommentResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({MyBatisUserRepository.class, CommentQueryService.class})
public class CommentQueryServiceTest extends DbTestBase {
  @Autowired private UserRepository userRepository;

  @Autowired private CommentQueryService commentQueryService;

  @MockBean private CommentServiceClient commentServiceClient;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
    userRepository.save(user);
  }

  @Test
  public void should_read_comment_success() {
    CommentResponse response = new CommentResponse();
    response.setId("comment-id-1");
    response.setBody("content");
    response.setUserId(user.getId());
    response.setArticleId("123");
    response.setCreatedAt("2024-01-01T00:00:00.000Z");
    response.setUpdatedAt("2024-01-01T00:00:00.000Z");

    when(commentServiceClient.getCommentById(eq("comment-id-1"))).thenReturn(Optional.of(response));

    Optional<CommentData> optional = commentQueryService.findById("comment-id-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals(commentData.getProfileData().getUsername(), user.getUsername());
  }

  @Test
  public void should_read_comments_of_article() {
    User user2 = new User("user2@email.com", "user2", "123", "", "");
    userRepository.save(user2);
    userRepository.saveRelation(new FollowRelation(user.getId(), user2.getId()));

    CommentResponse response1 = new CommentResponse();
    response1.setId("comment-id-1");
    response1.setBody("content1");
    response1.setUserId(user.getId());
    response1.setArticleId("article-1");
    response1.setCreatedAt("2024-01-01T00:00:00.000Z");
    response1.setUpdatedAt("2024-01-01T00:00:00.000Z");

    CommentResponse response2 = new CommentResponse();
    response2.setId("comment-id-2");
    response2.setBody("content2");
    response2.setUserId(user2.getId());
    response2.setArticleId("article-1");
    response2.setCreatedAt("2024-01-02T00:00:00.000Z");
    response2.setUpdatedAt("2024-01-02T00:00:00.000Z");

    when(commentServiceClient.getCommentsByArticleId(eq("article-1")))
        .thenReturn(Arrays.asList(response1, response2));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(comments.size(), 2);
  }
}
