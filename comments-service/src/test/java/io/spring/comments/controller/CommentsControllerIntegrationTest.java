package io.spring.comments.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentsControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Test comment");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("Test comment")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")));
  }

  @Test
  void shouldCreateCommentWithProvidedId() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("id", "custom-id-123");
    request.put("body", "Test comment");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", equalTo("custom-id-123")));
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    createComment("Comment 1", "user-1", "article-1");
    createComment("Comment 2", "user-2", "article-1");
    createComment("Comment 3", "user-1", "article-2");

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void shouldGetCommentById() throws Exception {
    MvcResult result = createComment("Comment body", "user-1", "article-1");
    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(get("/api/comments/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("Comment body")));
  }

  @Test
  void shouldGetCommentByIdAndArticleId() throws Exception {
    MvcResult result = createComment("Comment body", "user-1", "article-1");
    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(get("/api/comments/{id}", id).param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("Comment body")));
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/non-existent")).andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteComment() throws Exception {
    MvcResult result = createComment("To delete", "user-1", "article-1");
    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(delete("/api/comments/{id}", id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenDeletingNonExistentComment() throws Exception {
    mockMvc.perform(delete("/api/comments/non-existent")).andExpect(status().isNotFound());
  }

  private MvcResult createComment(String body, String userId, String articleId) throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);
    request.put("articleId", articleId);

    return mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();
  }
}
