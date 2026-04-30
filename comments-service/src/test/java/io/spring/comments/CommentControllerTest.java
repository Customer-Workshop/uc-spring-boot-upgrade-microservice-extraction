package io.spring.comments;

import static org.hamcrest.Matchers.equalTo;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateAndRetrieveComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Test comment body");
    request.put("userId", "user-1");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments/articles/article-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.comment.id", notNullValue()))
            .andExpect(jsonPath("$.comment.body", equalTo("Test comment body")))
            .andExpect(jsonPath("$.comment.userId", equalTo("user-1")))
            .andExpect(jsonPath("$.comment.articleId", equalTo("article-1")))
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    Map<String, Map<String, String>> responseMap = objectMapper.readValue(responseJson, Map.class);
    String commentId = responseMap.get("comment").get("id");

    mockMvc
        .perform(get("/api/comments/" + commentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment.id", equalTo(commentId)))
        .andExpect(jsonPath("$.comment.body", equalTo("Test comment body")));
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    mockMvc
        .perform(get("/api/comments/articles/article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments").isArray());
  }

  @Test
  void shouldGetCommentByArticleIdAndId() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Another test comment");
    request.put("userId", "user-2");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments/articles/article-2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    Map<String, Map<String, String>> responseMap = objectMapper.readValue(responseJson, Map.class);
    String commentId = responseMap.get("comment").get("id");

    mockMvc
        .perform(get("/api/comments/articles/article-2/" + commentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment.id", equalTo(commentId)))
        .andExpect(jsonPath("$.comment.body", equalTo("Another test comment")));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Comment to delete");
    request.put("userId", "user-1");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments/articles/article-3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    Map<String, Map<String, String>> responseMap = objectMapper.readValue(responseJson, Map.class);
    String commentId = responseMap.get("comment").get("id");

    mockMvc.perform(delete("/api/comments/" + commentId)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/" + commentId)).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/non-existent-id")).andExpect(status().isNotFound());
  }
}
