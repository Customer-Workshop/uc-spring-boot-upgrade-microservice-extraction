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
public class CommentsServiceIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void should_create_and_retrieve_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "integration test comment");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.body", equalTo("integration test comment")))
            .andExpect(jsonPath("$.userId", equalTo("user-1")))
            .andExpect(jsonPath("$.articleId", equalTo("article-1")))
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
    String commentId = (String) response.get("id");

    mockMvc
        .perform(get("/api/comments/{id}", commentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(commentId)))
        .andExpect(jsonPath("$.body", equalTo("integration test comment")));
  }

  @Test
  public void should_list_comments_by_article() throws Exception {
    String articleId = "article-list-test";

    Map<String, String> request1 = new HashMap<>();
    request1.put("body", "comment one");
    request1.put("userId", "user-1");
    request1.put("articleId", articleId);

    Map<String, String> request2 = new HashMap<>();
    request2.put("body", "comment two");
    request2.put("userId", "user-2");
    request2.put("articleId", articleId);

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
  public void should_delete_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "to be deleted");
    request.put("userId", "user-1");
    request.put("articleId", "article-del");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    @SuppressWarnings("unchecked")
    Map<String, Object> response = objectMapper.readValue(responseJson, Map.class);
    String commentId = (String) response.get("id");

    mockMvc.perform(delete("/api/comments/{id}", commentId)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", commentId)).andExpect(status().isNotFound());
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc.perform(get("/api/comments/nonexistent-id")).andExpect(status().isNotFound());
  }
}
