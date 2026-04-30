package io.spring.comments;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.dto.CommentRequest;
import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
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
class CommentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CommentRepository commentRepository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
  }

  @Test
  void shouldCreateComment() throws Exception {
    CommentRequest request = new CommentRequest("Test comment", "article-1", "user-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body", is("Test comment")))
        .andExpect(jsonPath("$.articleId", is("article-1")))
        .andExpect(jsonPath("$.userId", is("user-1")))
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void shouldGetCommentsByArticle() throws Exception {
    commentRepository.save(new Comment("Comment 1", "article-1", "user-1"));
    commentRepository.save(new Comment("Comment 2", "article-1", "user-2"));
    commentRepository.save(new Comment("Comment 3", "article-2", "user-1"));

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void shouldGetCommentById() throws Exception {
    Comment comment = commentRepository.save(new Comment("Test", "article-1", "user-1"));

    mockMvc
        .perform(get("/api/comments/{id}", comment.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", is("Test")));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    Comment comment = commentRepository.save(new Comment("Test", "article-1", "user-1"));

    mockMvc
        .perform(delete("/api/comments/{id}", comment.getId()))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", comment.getId())).andExpect(status().isNotFound());
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc.perform(get("/api/comments/nonexistent")).andExpect(status().isNotFound());
  }
}
