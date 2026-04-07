package io.spring.comments.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void should_create_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Test comment body");
    request.put("userId", "user-123");
    request.put("articleId", "article-456");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body").value("Test comment body"))
        .andExpect(jsonPath("$.userId").value("user-123"))
        .andExpect(jsonPath("$.articleId").value("article-456"))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.createdAt").isNotEmpty());
  }

  @Test
  public void should_create_comment_with_provided_id() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("id", "custom-id-789");
    request.put("body", "Comment with custom ID");
    request.put("userId", "user-111");
    request.put("articleId", "article-222");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("custom-id-789"))
        .andExpect(jsonPath("$.body").value("Comment with custom ID"));
  }

  @Test
  public void should_get_comments_by_article_id() throws Exception {
    // Create two comments for the same article
    Map<String, String> request1 = new HashMap<>();
    request1.put("body", "First comment");
    request1.put("userId", "user-1");
    request1.put("articleId", "article-get-test");

    Map<String, String> request2 = new HashMap<>();
    request2.put("body", "Second comment");
    request2.put("userId", "user-2");
    request2.put("articleId", "article-get-test");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/comments/article/article-get-test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  public void should_get_comment_by_id_and_article_id() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("id", "find-by-id-test");
    request.put("body", "Findable comment");
    request.put("userId", "user-find");
    request.put("articleId", "article-find");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/comments/find-by-id-test/article/article-find"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("find-by-id-test"))
        .andExpect(jsonPath("$.body").value("Findable comment"));
  }

  @Test
  public void should_get_comment_by_id_only() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("id", "by-id-only-test");
    request.put("body", "By ID only comment");
    request.put("userId", "user-byid");
    request.put("articleId", "article-byid");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/comments/by-id/by-id-only-test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("by-id-only-test"))
        .andExpect(jsonPath("$.body").value("By ID only comment"));
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc
        .perform(get("/api/comments/by-id/nonexistent"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void should_delete_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("id", "delete-test-id");
    request.put("body", "To be deleted");
    request.put("userId", "user-del");
    request.put("articleId", "article-del");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(delete("/api/comments/delete-test-id"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/comments/by-id/delete-test-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void should_reject_empty_body() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
