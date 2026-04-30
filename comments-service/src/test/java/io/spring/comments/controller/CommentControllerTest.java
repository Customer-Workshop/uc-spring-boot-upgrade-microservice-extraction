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
import io.spring.comments.dto.CreateCommentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateComment() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest(null, "Test comment", "user-1", "article-1");

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
    CreateCommentRequest request =
        new CreateCommentRequest("custom-id-123", "Comment with custom id", "user-1", "article-2");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", equalTo("custom-id-123")))
        .andExpect(jsonPath("$.body", equalTo("Comment with custom id")));
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    String articleId = "article-get-test";
    CreateCommentRequest request1 =
        new CreateCommentRequest(null, "Comment 1", "user-1", articleId);
    CreateCommentRequest request2 =
        new CreateCommentRequest(null, "Comment 2", "user-2", articleId);

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)));
    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)));

    mockMvc
        .perform(get("/api/comments").param("articleId", articleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void shouldGetCommentById() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("get-by-id-test", "Find me", "user-1", "article-5");

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

    mockMvc
        .perform(get("/api/comments/get-by-id-test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo("get-by-id-test")))
        .andExpect(jsonPath("$.body", equalTo("Find me")));
  }

  @Test
  void shouldGetCommentByIdAndArticleId() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("get-by-id-article-test", "Find me", "user-1", "article-6");

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

    mockMvc
        .perform(get("/api/comments/get-by-id-article-test").param("articleId", "article-6"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo("get-by-id-article-test")));

    mockMvc
        .perform(get("/api/comments/get-by-id-article-test").param("articleId", "wrong-article"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteComment() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("delete-test", "Delete me", "user-1", "article-7");

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)));

    mockMvc.perform(delete("/api/comments/delete-test")).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/delete-test")).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/non-existent")).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404WhenDeletingNonExistentComment() throws Exception {
    mockMvc.perform(delete("/api/comments/non-existent")).andExpect(status().isNotFound());
  }
}
