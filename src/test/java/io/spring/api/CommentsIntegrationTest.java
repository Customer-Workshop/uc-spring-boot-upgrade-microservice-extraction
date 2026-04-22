package io.spring.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
public class CommentsIntegrationTest {

  @Autowired private CommentServiceClient commentServiceClient;

  @BeforeEach
  public void checkServiceAvailable() {
    try {
      new RestTemplate()
          .getForEntity("http://localhost:8081/api/comments/article/ping", String.class);
    } catch (Exception e) {
      assumeTrue(false, "Comments microservice is not running at localhost:8081");
    }
  }

  @Test
  public void should_create_and_retrieve_comment_via_service() {
    String userId = "test-user-id";
    String articleId = "test-article-id";
    String body = "Integration test comment";

    CommentServiceResponse created =
        commentServiceClient.createComment(body, userId, articleId);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getBody()).isEqualTo(body);
    assertThat(created.getUserId()).isEqualTo(userId);
    assertThat(created.getArticleId()).isEqualTo(articleId);

    List<CommentServiceResponse> comments =
        commentServiceClient.getCommentsByArticle(articleId);
    assertThat(comments).isNotEmpty();
    assertThat(comments.stream().anyMatch(c -> c.getId().equals(created.getId()))).isTrue();

    CommentServiceResponse fetched =
        commentServiceClient.getComment(created.getId(), articleId);
    assertThat(fetched).isNotNull();
    assertThat(fetched.getId()).isEqualTo(created.getId());
    assertThat(fetched.getBody()).isEqualTo(body);
  }

  @Test
  public void should_create_and_delete_comment_via_service() {
    String userId = "test-user-id-2";
    String articleId = "test-article-id-2";
    String body = "Comment to be deleted";

    CommentServiceResponse created =
        commentServiceClient.createComment(body, userId, articleId);
    assertThat(created).isNotNull();

    commentServiceClient.deleteComment(created.getId(), articleId);

    List<CommentServiceResponse> remaining =
        commentServiceClient.getCommentsByArticle(articleId);
    assertThat(remaining.stream().noneMatch(c -> c.getId().equals(created.getId()))).isTrue();
  }

  @Test
  public void should_return_empty_list_for_article_with_no_comments() {
    List<CommentServiceResponse> comments =
        commentServiceClient.getCommentsByArticle("nonexistent-article-id");
    assertThat(comments).isEmpty();
  }
}
