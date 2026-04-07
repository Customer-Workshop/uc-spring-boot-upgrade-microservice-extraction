package io.spring.comments;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.api.CommentRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void should_create_and_get_comment() throws Exception {
    String commentId = UUID.randomUUID().toString();
    String articleId = "article-1";
    CommentRequest request = new CommentRequest(commentId, "Test comment", "user-1", articleId);

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", equalTo(commentId)))
        .andExpect(jsonPath("$.body", equalTo("Test comment")));

    mockMvc
        .perform(get("/api/comments/{id}", commentId).param("articleId", articleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(commentId)));
  }

  @Test
  public void should_get_comments_by_article_id() throws Exception {
    String articleId = "article-list-test-" + UUID.randomUUID();
    String id1 = UUID.randomUUID().toString();
    String id2 = UUID.randomUUID().toString();

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(
                    new CommentRequest(id1, "Comment 1", "user-1", articleId))));

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(
                objectMapper.writeValueAsString(
                    new CommentRequest(id2, "Comment 2", "user-2", articleId))));

    mockMvc
        .perform(get("/api/comments").param("articleId", articleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void should_delete_comment() throws Exception {
    String commentId = UUID.randomUUID().toString();
    CommentRequest request =
        new CommentRequest(commentId, "To be deleted", "user-1", "article-del");

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

    mockMvc.perform(delete("/api/comments/{id}", commentId)).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/comments/{id}", commentId).param("articleId", "article-del"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc
        .perform(get("/api/comments/{id}", "nonexistent").param("articleId", "any"))
        .andExpect(status().isNotFound());
  }
}
