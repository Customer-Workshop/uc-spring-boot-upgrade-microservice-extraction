package io.spring.comments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.controller.dto.CreateCommentRequest;
import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommentsServiceApplicationTests {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private CommentRepository commentRepository;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
  }

  @Test
  void contextLoads() {}

  @Test
  void shouldCreateComment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest("Test comment", "user-1");

    mockMvc
        .perform(
            post("/api/articles/article-1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.body").value("Test comment"))
        .andExpect(jsonPath("$.userId").value("user-1"))
        .andExpect(jsonPath("$.articleId").value("article-1"));

    assertThat(commentRepository.count()).isEqualTo(1);
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    Comment comment = new Comment("First comment", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc
        .perform(get("/api/articles/article-1/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].body").value("First comment"));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    Comment comment = new Comment("To delete", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc
        .perform(delete("/api/articles/article-1/comments/" + comment.getId()))
        .andExpect(status().isNoContent());

    assertThat(commentRepository.count()).isEqualTo(0);
  }

  @Test
  void shouldReturn404ForNonExistentComment() throws Exception {
    mockMvc
        .perform(get("/api/articles/article-1/comments/nonexistent"))
        .andExpect(status().isNotFound());
  }
}
