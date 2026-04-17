package io.spring.article.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.article.api.dto.CreateArticleRequest;
import io.spring.article.api.dto.CreateCommentRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateAndRetrieveComments() throws Exception {
    CreateArticleRequest articleRequest =
        CreateArticleRequest.builder()
            .title("Commented Article")
            .description("Description")
            .body("Body")
            .tagList(List.of())
            .userId("user-comment-test")
            .build();

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleRequest)))
        .andExpect(status().isCreated());

    CreateCommentRequest commentRequest =
        CreateCommentRequest.builder().body("Great article!").userId("commenter-1").build();

    mockMvc
        .perform(
            post("/api/articles/commented-article/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body").value("Great article!"))
        .andExpect(jsonPath("$.userId").value("commenter-1"));

    mockMvc
        .perform(get("/api/articles/commented-article/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].body").value("Great article!"));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    CreateArticleRequest articleRequest =
        CreateArticleRequest.builder()
            .title("Article With Deletable Comment")
            .description("Description")
            .body("Body")
            .tagList(List.of())
            .userId("user-delete-comment")
            .build();

    mockMvc
        .perform(
            post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(articleRequest)))
        .andExpect(status().isCreated());

    CreateCommentRequest commentRequest =
        CreateCommentRequest.builder().body("Delete me").userId("commenter-2").build();

    String response =
        mockMvc
            .perform(
                post("/api/articles/article-with-deletable-comment/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(commentRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String commentId = objectMapper.readTree(response).get("id").asText();

    mockMvc
        .perform(delete("/api/articles/article-with-deletable-comment/comments/" + commentId))
        .andExpect(status().isNoContent());
  }
}
