package io.spring.comments.api;

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
  public void shouldCreateComment() throws Exception {
    Map<String, String> param = new HashMap<>();
    param.put("body", "This is a test comment");
    param.put("userId", "user-1");

    mockMvc
        .perform(
            post("/api/articles/article-1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.body").value("This is a test comment"))
        .andExpect(jsonPath("$.comment.id").isNotEmpty());
  }

  @Test
  public void shouldGetComments() throws Exception {
    Map<String, String> param = new HashMap<>();
    param.put("body", "Comment for listing");
    param.put("userId", "user-2");

    mockMvc.perform(
        post("/api/articles/article-2/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(param)));

    mockMvc
        .perform(get("/api/articles/article-2/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments").isArray());
  }

  @Test
  public void shouldDeleteComment() throws Exception {
    Map<String, String> param = new HashMap<>();
    param.put("body", "Comment to delete");
    param.put("userId", "user-3");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/articles/article-3/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(param)))
            .andExpect(status().isCreated())
            .andReturn();

    String responseJson = result.getResponse().getContentAsString();
    String commentId =
        objectMapper.readTree(responseJson).get("comment").get("id").asText();

    mockMvc
        .perform(delete("/api/articles/article-3/comments/" + commentId))
        .andExpect(status().isNoContent());
  }

  @Test
  public void shouldReturnNotFoundForNonExistentComment() throws Exception {
    mockMvc
        .perform(delete("/api/articles/article-99/comments/non-existent-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void shouldRejectEmptyBody() throws Exception {
    Map<String, String> param = new HashMap<>();
    param.put("body", "");
    param.put("userId", "user-4");

    mockMvc
        .perform(
            post("/api/articles/article-4/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(status().isBadRequest());
  }
}
