package io.spring.comments;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.api.CreateCommentRequest;
import io.spring.comments.domain.Comment;
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
class CommentsControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private CommentRepository commentRepository;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
  }

  @Test
  void shouldCreateComment() throws Exception {
    CreateCommentRequest request = new CreateCommentRequest();
    request.setBody("Test comment");
    request.setUserId("user-1");
    request.setArticleId("article-1");

    mockMvc
        .perform(
            post("/api/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.body", is("Test comment")))
        .andExpect(jsonPath("$.userId", is("user-1")))
        .andExpect(jsonPath("$.articleId", is("article-1")));
  }

  @Test
  void shouldGetCommentsByArticle() throws Exception {
    Comment comment1 = new Comment("Comment 1", "user-1", "article-1");
    Comment comment2 = new Comment("Comment 2", "user-2", "article-1");
    commentRepository.save(comment1);
    commentRepository.save(comment2);

    mockMvc
        .perform(get("/api/comments").param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void shouldGetCommentById() throws Exception {
    Comment comment = new Comment("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc
        .perform(get("/api/comments/" + comment.getId()).param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", is("Test comment")));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    Comment comment = new Comment("Test comment", "user-1", "article-1");
    commentRepository.save(comment);

    mockMvc.perform(delete("/api/comments/" + comment.getId())).andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn404ForNonexistentComment() throws Exception {
    mockMvc
        .perform(get("/api/comments/nonexistent").param("articleId", "article-1"))
        .andExpect(status().isNotFound());
  }
}
