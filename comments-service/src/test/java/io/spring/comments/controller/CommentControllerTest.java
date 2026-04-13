package io.spring.comments.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.controller.dto.CreateCommentRequest;
import io.spring.comments.model.CommentEntity;
import io.spring.comments.repository.CommentJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private CommentJpaRepository commentRepository;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
  }

  @Test
  void shouldCreateComment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest(null, "Test comment", "user-1", "article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("Test comment")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")));
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    CommentEntity comment1 = new CommentEntity("Comment 1", "user-1", "article-1");
    CommentEntity comment2 = new CommentEntity("Comment 2", "user-2", "article-1");
    commentRepository.save(comment1);
    commentRepository.save(comment2);

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void shouldGetCommentById() throws Exception {
    CommentEntity comment = new CommentEntity("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc
        .perform(get("/api/comments/{id}", comment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("Test comment")));
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/{id}", "non-existent")).andExpect(status().isNotFound());
  }

  @Test
  void shouldDeleteComment() throws Exception {
    CommentEntity comment = new CommentEntity("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc.perform(delete("/api/comments/{id}", comment.getId())).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", comment.getId())).andExpect(status().isNotFound());
  }

  @Test
  void shouldGetCommentByIdAndArticleId() throws Exception {
    CommentEntity comment = new CommentEntity("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc
        .perform(get("/api/comments/by-article/{articleId}/{id}", "article-1", comment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("Test comment")));
  }
}
