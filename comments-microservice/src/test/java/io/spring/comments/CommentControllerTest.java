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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  public void should_create_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Test comment body");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.id", notNullValue()))
        .andExpect(jsonPath("$.comment.body", equalTo("Test comment body")))
        .andExpect(jsonPath("$.comment.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.comment.articleId", equalTo("article-1")));
  }

  @Test
  public void should_get_comment_by_id() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Comment to find");
    request.put("userId", "user-2");
    request.put("articleId", "article-2");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    String id = objectMapper.readTree(responseBody).get("comment").get("id").asText();

    mockMvc
        .perform(get("/api/comments/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment.id", equalTo(id)))
        .andExpect(jsonPath("$.comment.body", equalTo("Comment to find")));
  }

  @Test
  public void should_get_comments_by_article_id() throws Exception {
    String articleId = "article-list-test";

    Map<String, String> request1 = new HashMap<>();
    request1.put("body", "First comment");
    request1.put("userId", "user-3");
    request1.put("articleId", articleId);

    Map<String, String> request2 = new HashMap<>();
    request2.put("body", "Second comment");
    request2.put("userId", "user-4");
    request2.put("articleId", articleId);

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/comments/article/" + articleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(2)));
  }

  @Test
  public void should_delete_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "Comment to delete");
    request.put("userId", "user-5");
    request.put("articleId", "article-del");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    String id = objectMapper.readTree(responseBody).get("comment").get("id").asText();

    mockMvc.perform(delete("/api/comments/" + id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/" + id)).andExpect(status().isNotFound());
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc.perform(get("/api/comments/nonexistent-id")).andExpect(status().isNotFound());
  }

  @Test
  public void should_return_400_for_empty_body() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "");
    request.put("userId", "user-6");
    request.put("articleId", "article-6");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
