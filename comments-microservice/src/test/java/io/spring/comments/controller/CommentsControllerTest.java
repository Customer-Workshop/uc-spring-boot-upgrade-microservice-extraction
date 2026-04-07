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
class CommentsControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CommentRepository commentRepository;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
  }

  @Test
  void should_create_comment() throws Exception {
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
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.body", equalTo("Test comment")))
        .andExpect(jsonPath("$.userId", equalTo("user-1")))
        .andExpect(jsonPath("$.articleId", equalTo("article-1")));
  }

  @Test
  void should_get_comments_by_article_id() throws Exception {
    Comment c1 = new Comment("comment 1", "user-1", "article-1");
    Comment c2 = new Comment("comment 2", "user-2", "article-1");
    Comment c3 = new Comment("other article comment", "user-1", "article-2");
    commentRepository.save(c1);
    commentRepository.save(c2);
    commentRepository.save(c3);

    mockMvc
        .perform(get("/api/comments/article/article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)));
  }

  @Test
  void should_get_comment_by_id() throws Exception {
    Comment c = new Comment("test body", "user-1", "article-1");
    commentRepository.save(c);

    mockMvc
        .perform(get("/api/comments/{id}", c.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("test body")));
  }

  @Test
  void should_get_comment_by_id_with_article_filter() throws Exception {
    Comment c = new Comment("test body", "user-1", "article-1");
    commentRepository.save(c);

    mockMvc
        .perform(get("/api/comments/{id}", c.getId()).param("articleId", "article-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.body", equalTo("test body")));

    mockMvc
        .perform(get("/api/comments/{id}", c.getId()).param("articleId", "wrong-article"))
        .andExpect(status().isNotFound());
  }

  @Test
  void should_delete_comment() throws Exception {
    Comment c = new Comment("to delete", "user-1", "article-1");
    commentRepository.save(c);

    mockMvc.perform(delete("/api/comments/{id}", c.getId())).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/comments/{id}", c.getId())).andExpect(status().isNotFound());
  }

  @Test
  void should_return_404_when_deleting_nonexistent_comment() throws Exception {
    mockMvc.perform(delete("/api/comments/nonexistent")).andExpect(status().isNotFound());
  }
}
