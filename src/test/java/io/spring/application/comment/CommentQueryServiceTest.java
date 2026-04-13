package io.spring.application.comment;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.core.user.User;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @Mock private UserRelationshipQueryService userRelationshipQueryService;

  @InjectMocks private CommentQueryService commentQueryService;

  private User user;

  @BeforeEach
  public void setUp() {
    user = new User("aisensiy@test.com", "aisensiy", "123", "", "");
  }

  @Test
  public void should_read_comment_success() {
    Map<String, Object> rawComment = new HashMap<>();
    rawComment.put("id", "comment-1");
    rawComment.put("body", "content");
    rawComment.put("userId", user.getId());
    rawComment.put("articleId", "123");
    rawComment.put("createdAt", "2026-01-01T00:00:00Z");
    rawComment.put("updatedAt", "2026-01-01T00:00:00Z");

    when(commentServiceClient.findCommentById("comment-1")).thenReturn(Optional.of(rawComment));
    when(userRelationshipQueryService.isUserFollowing(anyString(), anyString())).thenReturn(false);

    Optional<CommentData> optional = commentQueryService.findById("comment-1", user);
    Assertions.assertTrue(optional.isPresent());
    CommentData commentData = optional.get();
    Assertions.assertEquals("content", commentData.getBody());
  }

  @Test
  public void should_read_comments_of_article() {
    Map<String, Object> raw1 = new HashMap<>();
    raw1.put("id", "comment-1");
    raw1.put("body", "content1");
    raw1.put("userId", user.getId());
    raw1.put("articleId", "article-1");
    raw1.put("createdAt", "2026-01-01T00:00:00Z");
    raw1.put("updatedAt", "2026-01-01T00:00:00Z");

    Map<String, Object> raw2 = new HashMap<>();
    raw2.put("id", "comment-2");
    raw2.put("body", "content2");
    raw2.put("userId", "user-2");
    raw2.put("articleId", "article-1");
    raw2.put("createdAt", "2026-01-02T00:00:00Z");
    raw2.put("updatedAt", "2026-01-02T00:00:00Z");

    when(commentServiceClient.findByArticleId("article-1")).thenReturn(Arrays.asList(raw1, raw2));

    List<CommentData> comments = commentQueryService.findByArticleId("article-1", user);
    Assertions.assertEquals(2, comments.size());
  }
}
