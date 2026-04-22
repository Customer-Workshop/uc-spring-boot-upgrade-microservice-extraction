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
class CommentsControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void should_create_comment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest("Test comment body", "user-1", "article-1");
    mvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("Test comment body")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")));
  }

  @Test
  void should_get_comment_by_id() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("Comment to retrieve", "user-2", "article-2");
    MvcResult createResult =
        mvc.perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    CommentDto created =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), CommentDto.class);

    mvc.perform(get("/api/comments/{id}", created.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(created.getId())))
        .andExpect(jsonPath("$.body", equalTo("Comment to retrieve")));
  }

  @Test
  void should_get_comments_by_article_id() throws Exception {
    String articleId = "article-list-test";
    CreateCommentRequest request1 =
        new CreateCommentRequest("First comment", "user-1", articleId);
    CreateCommentRequest request2 =
        new CreateCommentRequest("Second comment", "user-2", articleId);

    mvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
        .andExpect(status().isCreated());
    mvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
        .andExpect(status().isCreated());

    mvc.perform(get("/api/comments").param("articleId", articleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void should_delete_comment() throws Exception {
    CreateCommentRequest request =
        new CreateCommentRequest("Comment to delete", "user-1", "article-del");
    MvcResult createResult =
        mvc.perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    CommentDto created =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), CommentDto.class);

    mvc.perform(delete("/api/comments/{id}", created.getId()))
        .andExpect(status().isNoContent());

    mvc.perform(get("/api/comments/{id}", created.getId()))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_return_404_for_nonexistent_comment() throws Exception {
    mvc.perform(get("/api/comments/{id}", "nonexistent-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_get_comment_by_article_and_id() throws Exception {
    String articleId = "article-by-id-test";
    CreateCommentRequest request =
        new CreateCommentRequest("Article-specific comment", "user-1", articleId);
    MvcResult createResult =
        mvc.perform(
                post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

    CommentDto created =
        objectMapper.readValue(createResult.getResponse().getContentAsString(), CommentDto.class);

    mvc.perform(get("/api/comments/by-article/{articleId}/{commentId}", articleId, created.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", equalTo(created.getId())))
        .andExpect(jsonPath("$.articleId", equalTo(articleId)));
  }

  @Test
  void should_reject_empty_body() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest("", "user-1", "article-1");
    mvc.perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
