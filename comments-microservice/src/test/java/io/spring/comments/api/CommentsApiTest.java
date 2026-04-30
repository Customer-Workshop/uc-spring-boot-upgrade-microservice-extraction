package io.spring.comments.api;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentsApiTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  public void should_create_and_get_comment() throws Exception {
    String articleId = "article-1";

    Map<String, String> param = new HashMap<>();
    param.put("body", "This is a test comment");
    param.put("userId", "user-1");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/articles/{articleId}/comments", articleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(param)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.comment.id", notNullValue()))
            .andExpect(jsonPath("$.comment.body", equalTo("This is a test comment")))
            .andExpect(jsonPath("$.comment.articleId", equalTo(articleId)))
            .andExpect(jsonPath("$.comment.userId", equalTo("user-1")))
            .andReturn();

    mockMvc
        .perform(get("/api/articles/{articleId}/comments", articleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments[0].body", equalTo("This is a test comment")));
  }

  @Test
  public void should_delete_comment() throws Exception {
    String articleId = "article-2";

    Map<String, String> param = new HashMap<>();
    param.put("body", "Comment to delete");
    param.put("userId", "user-1");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/articles/{articleId}/comments", articleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(param)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    String commentId = objectMapper.readTree(responseBody).get("comment").get("id").asText();

    mockMvc
        .perform(delete("/api/articles/{articleId}/comments/{commentId}", articleId, commentId))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/articles/{articleId}/comments/{commentId}", articleId, commentId))
        .andExpect(status().isNotFound());
  }

  @Test
  public void should_get_comment_by_id() throws Exception {
    String articleId = "article-3";

    Map<String, String> param = new HashMap<>();
    param.put("body", "Lookup by ID test");
    param.put("userId", "user-2");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/articles/{articleId}/comments", articleId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(param)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    String commentId = objectMapper.readTree(responseBody).get("comment").get("id").asText();

    mockMvc
        .perform(get("/api/comments/{commentId}", commentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comment.body", equalTo("Lookup by ID test")))
        .andExpect(jsonPath("$.comment.userId", equalTo("user-2")));
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc
        .perform(get("/api/comments/{commentId}", "nonexistent-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void should_return_empty_comments_for_article_with_no_comments() throws Exception {
    mockMvc
        .perform(get("/api/articles/{articleId}/comments", "no-comments-article"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments").isArray())
        .andExpect(jsonPath("$.comments").isEmpty());
  }
}
