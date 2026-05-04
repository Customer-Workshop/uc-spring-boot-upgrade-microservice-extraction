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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  public void should_create_comment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "This is a comment");
    request.put("userId", "user-1");
    request.put("articleId", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("This is a comment")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")))
        .andExpect(jsonPath("$.createdAt", notNullValue()));
  }

  @Test
  public void should_get_comments_by_article() throws Exception {
    createComment("Comment 1", "user-1", "article-1");
    createComment("Comment 2", "user-2", "article-1");
    createComment("Comment 3", "user-1", "article-2");

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  public void should_get_comment_by_id() throws Exception {
    MvcResult result = createComment("My comment", "user-1", "article-1");
    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(get("/api/comments/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("My comment")));
  }

  @Test
  public void should_delete_comment() throws Exception {
    MvcResult result = createComment("To delete", "user-1", "article-1");
    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(delete("/api/comments/{id}", id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc.perform(get("/api/comments/{id}", "nonexistent")).andExpect(status().isNotFound());
  }

  private MvcResult createComment(String body, String userId, String articleId) throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", body);
    request.put("userId", userId);
    request.put("articleId", articleId);

    return mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andReturn();
  }
}
