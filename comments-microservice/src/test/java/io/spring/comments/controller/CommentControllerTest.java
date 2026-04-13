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
        .andExpect(jsonPath("$.comment.body").value("Test comment"))
        .andExpect(jsonPath("$.comment.userId").value("user-1"))
        .andExpect(jsonPath("$.comment.articleId").value("article-1"))
        .andExpect(jsonPath("$.comment.id").exists());
  }

  @Test
  void shouldGetCommentsByArticle() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Article comment");
    request.put("userId", "user-2");
    request.put("articleId", "article-99");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/comments/article/article-99"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments[0].body").value("Article comment"));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "To be deleted");
    request.put("userId", "user-3");
    request.put("articleId", "article-del");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    String commentId = objectMapper.readTree(responseBody).get("comment").get("id").asText();

    mockMvc.perform(delete("/api/comments/" + commentId)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/" + commentId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn400ForEmptyBody() throws Exception {
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
