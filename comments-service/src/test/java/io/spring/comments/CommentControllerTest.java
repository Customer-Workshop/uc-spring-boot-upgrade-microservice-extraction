package io.spring.comments;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.controller.CreateCommentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateComment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest("Test comment", "user-1", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", is("Test comment")))
        .andExpect(jsonPath("$.userId", is("user-1")))
        .andExpect(jsonPath("$.articleId", is("article-1")));
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("Article comment", "user-1", "article-get-test");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-get-test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].body", is("Article comment")));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("To be deleted", "user-1", "article-del-test");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(delete("/api/comments/" + id)).andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/comments/" + id).param("articleId", "article-del-test"))
        .andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc
        .perform(get("/api/comments/nonexistent").param("articleId", "article-1"))
        .andExpect(status().isNotFound());
  }
}
