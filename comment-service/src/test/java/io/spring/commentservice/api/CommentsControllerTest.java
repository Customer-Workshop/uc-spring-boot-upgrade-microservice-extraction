package io.spring.commentservice.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.commentservice.JacksonCustomizations;
import io.spring.commentservice.application.CommentService;
import io.spring.commentservice.application.dto.ArticleDto;
import io.spring.commentservice.application.dto.CommentData;
import io.spring.commentservice.application.dto.ProfileDto;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.infrastructure.client.MonolithClient;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentsController.class)
@Import(JacksonCustomizations.class)
public class CommentsControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockBean private CommentService commentService;
  @MockBean private MonolithClient monolithClient;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void shouldCreateComment() throws Exception {
    ArticleDto article = new ArticleDto("article-id", "test-slug", "Test Title");
    when(monolithClient.getArticleBySlug("test-slug")).thenReturn(article);

    Comment comment = new Comment("great article", "user1", "article-id");
    when(commentService.createComment(eq("great article"), eq("user1"), eq("article-id")))
        .thenReturn(comment);

    ProfileDto profile = new ProfileDto("user1", "testuser", "bio", "image.jpg", false);
    CommentData commentData =
        new CommentData(
            comment.getId(),
            "great article",
            "article-id",
            new DateTime(),
            new DateTime(),
            profile);
    when(commentService.findById(comment.getId())).thenReturn(Optional.of(commentData));

    Map<String, Object> body = new HashMap<>();
    Map<String, String> commentBody = new HashMap<>();
    commentBody.put("body", "great article");
    body.put("comment", commentBody);

    mockMvc
        .perform(
            post("/articles/test-slug/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", "user1")
                .content(objectMapper.writeValueAsString(body)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.body").value("great article"));
  }

  @Test
  void shouldGetComments() throws Exception {
    ArticleDto article = new ArticleDto("article-id", "test-slug", "Test Title");
    when(monolithClient.getArticleBySlug("test-slug")).thenReturn(article);

    ProfileDto profile = new ProfileDto("user1", "testuser", "bio", "image.jpg", false);
    CommentData commentData =
        new CommentData("comment1", "body1", "article-id", new DateTime(), new DateTime(), profile);
    when(commentService.findByArticleId("article-id")).thenReturn(Arrays.asList(commentData));

    mockMvc
        .perform(get("/articles/test-slug/comments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments[0].body").value("body1"));
  }

  @Test
  void shouldDeleteComment() throws Exception {
    ArticleDto article = new ArticleDto("article-id", "test-slug", "Test Title");
    when(monolithClient.getArticleBySlug("test-slug")).thenReturn(article);

    mockMvc
        .perform(delete("/articles/test-slug/comments/comment1"))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturn404WhenArticleNotFound() throws Exception {
    when(monolithClient.getArticleBySlug("nonexistent")).thenReturn(null);

    mockMvc.perform(get("/articles/nonexistent/comments")).andExpect(status().isNotFound());
  }
}
