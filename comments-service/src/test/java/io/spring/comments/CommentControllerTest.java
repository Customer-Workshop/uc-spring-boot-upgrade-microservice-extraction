package io.spring.comments;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.controller.dto.CreateCommentRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void shouldCreateComment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Test comment body");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("Test comment body")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")));
  }

  @Test
  public void shouldGetCommentsByArticleId() throws Exception {
    // First create a comment
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Comment for article listing");
    request.setUserId("user-2");
    request.setArticleId("article-list-test");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // Then get comments for that article
    mockMvc
        .perform(get("/api/comments/article/article-list-test"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].body", equalTo("Comment for article listing")));
  }

  @Test
  public void shouldDeleteComment() throws Exception {
    // First create a comment
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Comment to delete");
    request.setUserId("user-1");
    request.setArticleId("article-delete-test");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseJson = result.getResponse().getContentAsString();
    String commentId = objectMapper.readTree(responseJson).get("id").asText();

    // Then delete it
    mockMvc.perform(delete("/api/comments/" + commentId)).andExpect(status().isNoContent());

    // Verify it's gone
    mockMvc.perform(get("/api/comments/" + commentId)).andExpect(status().isNotFound());
  }

  @Test
  public void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/non-existent-id")).andExpect(status().isNotFound());
  }

  @Test
  public void shouldRejectEmptyBody() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
