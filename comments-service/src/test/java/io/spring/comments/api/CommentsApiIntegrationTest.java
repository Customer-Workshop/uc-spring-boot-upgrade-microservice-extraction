package io.spring.comments.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import io.spring.comments.CommentsServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = CommentsServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentsApiIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  public void should_create_comment_and_retrieve_it() throws Exception {
    String requestBody =
        "{\"body\": \"Test comment\", \"userId\": \"user-1\", \"articleId\": \"article-1\"}";

    MvcResult createResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andExpect(MockMvcResultMatchers.jsonPath("$.comment.id", notNullValue()))
            .andExpect(MockMvcResultMatchers.jsonPath("$.comment.body", equalTo("Test comment")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.comment.userId", equalTo("user-1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.comment.articleId", equalTo("article-1")))
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    String commentId =
        com.fasterxml.jackson.databind.ObjectMapper.class
            .getDeclaredConstructor()
            .newInstance()
            .readTree(responseJson)
            .get("comment")
            .get("id")
            .asText();

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/comments/{id}", commentId))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.comment.id", equalTo(commentId)))
        .andExpect(MockMvcResultMatchers.jsonPath("$.comment.body", equalTo("Test comment")));
  }

  @Test
  public void should_list_comments_by_article_id() throws Exception {
    String request1 =
        "{\"body\": \"Comment 1\", \"userId\": \"user-1\", \"articleId\": \"article-list-test\"}";
    String request2 =
        "{\"body\": \"Comment 2\", \"userId\": \"user-2\", \"articleId\": \"article-list-test\"}";

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request1))
        .andExpect(MockMvcResultMatchers.status().isCreated());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request2))
        .andExpect(MockMvcResultMatchers.status().isCreated());

    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/comments")
                .param("articleId", "article-list-test"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.comments", hasSize(2)));
  }

  @Test
  public void should_delete_comment() throws Exception {
    String requestBody =
        "{\"body\": \"To be deleted\", \"userId\": \"user-1\", \"articleId\": \"article-del\"}";

    MvcResult createResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(MockMvcResultMatchers.status().isCreated())
            .andReturn();

    String responseJson = createResult.getResponse().getContentAsString();
    String commentId =
        new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(responseJson)
            .get("comment")
            .get("id")
            .asText();

    mockMvc
        .perform(MockMvcRequestBuilders.delete("/api/comments/{id}", commentId))
        .andExpect(MockMvcResultMatchers.status().isNoContent());

    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/comments/{id}", commentId))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  public void should_return_404_for_nonexistent_comment() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get("/api/comments/nonexistent-id"))
        .andExpect(MockMvcResultMatchers.status().isNotFound());
  }

  @Test
  public void should_return_empty_list_for_article_with_no_comments() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get("/api/comments")
                .param("articleId", "nonexistent-article"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.comments", hasSize(0)));
  }
}
