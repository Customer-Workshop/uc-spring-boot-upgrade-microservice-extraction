package io.spring.comments.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsControllerTest {

  @Autowired private TestRestTemplate restTemplate;

  @Test
  @SuppressWarnings("unchecked")
  void should_create_and_retrieve_comment() {
    CreateCommentRequest request = new CreateCommentRequest();
    setField(request, "body", "Test comment");
    setField(request, "userId", "user-1");
    setField(request, "articleId", "article-1");

    ResponseEntity<Map> createResponse =
        restTemplate.postForEntity("/api/comments", request, Map.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    Map<String, Object> created = (Map<String, Object>) createResponse.getBody().get("comment");
    assertThat(created.get("body")).isEqualTo("Test comment");
    assertThat(created.get("userId")).isEqualTo("user-1");
    assertThat(created.get("articleId")).isEqualTo("article-1");

    String commentId = (String) created.get("id");

    ResponseEntity<Map> getResponse =
        restTemplate.getForEntity("/api/comments/{id}?articleId=article-1", Map.class, commentId);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    Map<String, Object> fetched = (Map<String, Object>) getResponse.getBody().get("comment");
    assertThat(fetched.get("id")).isEqualTo(commentId);
    assertThat(fetched.get("body")).isEqualTo("Test comment");
  }

  @Test
  @SuppressWarnings("unchecked")
  void should_list_comments_by_article_id() {
    String articleId = "article-list-" + System.currentTimeMillis();

    CreateCommentRequest req1 = new CreateCommentRequest();
    setField(req1, "body", "Comment A");
    setField(req1, "userId", "user-a");
    setField(req1, "articleId", articleId);
    restTemplate.postForEntity("/api/comments", req1, Map.class);

    CreateCommentRequest req2 = new CreateCommentRequest();
    setField(req2, "body", "Comment B");
    setField(req2, "userId", "user-b");
    setField(req2, "articleId", articleId);
    restTemplate.postForEntity("/api/comments", req2, Map.class);

    ResponseEntity<Map> response =
        restTemplate.getForEntity("/api/comments?articleId={articleId}", Map.class, articleId);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    List<Map<String, Object>> comments =
        (List<Map<String, Object>>) response.getBody().get("comments");
    assertThat(comments).hasSize(2);
  }

  @Test
  void should_delete_comment() {
    String articleId = "article-del-" + System.currentTimeMillis();

    CreateCommentRequest request = new CreateCommentRequest();
    setField(request, "body", "To delete");
    setField(request, "userId", "user-1");
    setField(request, "articleId", articleId);

    @SuppressWarnings("unchecked")
    Map<String, Object> created =
        (Map<String, Object>)
            restTemplate
                .postForEntity("/api/comments", request, Map.class)
                .getBody()
                .get("comment");
    String commentId = (String) created.get("id");

    restTemplate.delete("/api/comments/{id}", commentId);

    ResponseEntity<Map> getResponse =
        restTemplate.getForEntity(
            "/api/comments/{id}?articleId={articleId}", Map.class, commentId, articleId);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void should_return_404_for_nonexistent_comment() {
    ResponseEntity<Map> response =
        restTemplate.getForEntity("/api/comments/{id}?articleId=fake", Map.class, "nonexistent-id");
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  private void setField(Object obj, String fieldName, Object value) {
    try {
      java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(obj, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
