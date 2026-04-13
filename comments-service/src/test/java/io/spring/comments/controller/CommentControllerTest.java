package io.spring.comments.controller;

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
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateAndRetrieveComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Test comment body");
    request.put("userId", "user-123");
    request.put("articleId", "article-456");

    MvcResult result =
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
            .andReturn();

    String responseJson = result.getResponse().getContentAsString();
    String commentId = objectMapper.readTree(responseJson).get("id").asText();

    mockMvc
        .perform(get("/api/comments/{id}", commentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(commentId))
        .andExpect(jsonPath("$.body").value("Test comment body"));

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-456"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(commentId));

    mockMvc
        .perform(
            get("/api/comments/{id}/by-article", commentId).param("articleId", "article-456"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(commentId));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "To be deleted");
    request.put("userId", "user-123");
    request.put("articleId", "article-789");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String commentId =
        objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(delete("/api/comments/{id}", commentId)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", commentId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/nonexistent")).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturnEmptyListForNoComments() throws Exception {
    mockMvc
        .perform(get("/api/comments").param("articleId", "no-such-article"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());
  }
}
