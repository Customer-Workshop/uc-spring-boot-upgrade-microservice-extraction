package io.spring.comments.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    CreateCommentRequest request = new CreateCommentRequest("test body", "user-1", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("test body")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")));
  }

  @Test
  public void should_get_comments_by_article_id() throws Exception {
    CreateCommentRequest request1 = new CreateCommentRequest("comment one", "user-1", "article-10");
    CreateCommentRequest request2 = new CreateCommentRequest("comment two", "user-2", "article-10");

    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request1)));
    mockMvc.perform(
        post("/api/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request2)));

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void should_delete_comment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest("to delete", "user-1", "article-20");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    String id = objectMapper.readTree(responseBody).get("id").asText();

    mockMvc.perform(delete("/api/comments/" + id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/" + id)).andExpect(status().isNotFound());
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc.perform(get("/api/comments/nonexistent-id")).andExpect(status().isNotFound());
  }

  @Test
  public void should_return_400_for_empty_body() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest("", "user-1", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
