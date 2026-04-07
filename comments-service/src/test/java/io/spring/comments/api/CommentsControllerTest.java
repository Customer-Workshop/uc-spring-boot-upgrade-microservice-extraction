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
    Map<String, Object> param = new HashMap<>();
    param.put("body", "test comment body");
    param.put("userId", "user-1");

    mockMvc
        .perform(
            post("/api/articles/article-1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.body", equalTo("test comment body")))
        .andExpect(jsonPath("$.comment.id", notNullValue()));
  }

  @Test
  public void should_get_comments_by_article() throws Exception {
    // Create a comment first
    Map<String, Object> param = new HashMap<>();
    param.put("body", "comment for listing");
    param.put("userId", "user-2");

    mockMvc
        .perform(
            post("/api/articles/article-2/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(status().isCreated());

    // Get comments
    mockMvc
        .perform(get("/api/articles/article-2/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(1)))
        .andExpect(jsonPath("$.comments[0].body", equalTo("comment for listing")));
  }

  @Test
  public void should_delete_comment() throws Exception {
    // Create a comment first
    Map<String, Object> param = new HashMap<>();
    param.put("body", "comment to delete");
    param.put("userId", "user-3");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/articles/article-3/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(param)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    String commentId = objectMapper.readTree(responseBody).get("comment").get("id").asText();

    // Delete the comment
    mockMvc
        .perform(delete("/api/articles/article-3/comments/" + commentId))
        .andExpect(status().isNoContent());

    // Verify it's gone
    mockMvc
        .perform(get("/api/articles/article-3/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(0)));
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc
        .perform(delete("/api/articles/article-4/comments/nonexistent-id"))
        .andExpect(status().isNotFound());
  }
}
