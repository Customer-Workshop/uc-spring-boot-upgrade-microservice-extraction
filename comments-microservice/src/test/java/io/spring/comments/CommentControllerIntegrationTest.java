package io.spring.comments;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.comments.model.Comment;
import io.spring.comments.repository.CommentRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private CommentRepository commentRepository;

  @Autowired private ObjectMapper objectMapper;

  private static final String ARTICLE_ID = "test-article-1";
  private static final String USER_ID = "test-user-1";
  private static final String OTHER_USER_ID = "test-user-2";

  @BeforeEach
  void setUp() {
    commentRepository.deleteAll();
  }

  @Test
  void shouldCreateComment() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "This is a test comment");

    mockMvc
        .perform(
            post("/api/articles/{articleId}/comments", ARTICLE_ID)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.id", notNullValue()))
        .andExpect(jsonPath("$.comment.body", is("This is a test comment")))
        .andExpect(jsonPath("$.comment.userId", is(USER_ID)))
        .andExpect(jsonPath("$.comment.articleId", is(ARTICLE_ID)));
  }

  @Test
  void shouldGetCommentsByArticleId() throws Exception {
    Comment c1 = new Comment("First comment", USER_ID, ARTICLE_ID);
    Comment c2 = new Comment("Second comment", USER_ID, ARTICLE_ID);
    Comment other = new Comment("Other article", USER_ID, "other-article");
    commentRepository.save(c1);
    commentRepository.save(c2);
    commentRepository.save(other);

    mockMvc
        .perform(get("/api/articles/{articleId}/comments", ARTICLE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(2)));
  }

  @Test
  void shouldDeleteOwnComment() throws Exception {
    Comment comment = new Comment("My comment", USER_ID, ARTICLE_ID);
    commentRepository.save(comment);

    mockMvc
        .perform(
            delete("/api/articles/{articleId}/comments/{commentId}", ARTICLE_ID, comment.getId())
                .header("X-User-Id", USER_ID))
        .andExpect(status().isNoContent());

    assert commentRepository.findById(comment.getId()).isEmpty();
  }

  @Test
  void shouldForbidDeletingOtherUsersComment() throws Exception {
    Comment comment = new Comment("Not my comment", USER_ID, ARTICLE_ID);
    commentRepository.save(comment);

    mockMvc
        .perform(
            delete("/api/articles/{articleId}/comments/{commentId}", ARTICLE_ID, comment.getId())
                .header("X-User-Id", OTHER_USER_ID))
        .andExpect(status().isForbidden());

    assert commentRepository.findById(comment.getId()).isPresent();
  }

  @Test
  void shouldReturnEmptyListForUnknownArticle() throws Exception {
    mockMvc
        .perform(get("/api/articles/{articleId}/comments", "nonexistent"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(0)));
  }

  @Test
  void shouldRejectBlankCommentBody() throws Exception {
    Map<String, String> request = new HashMap<>();
    request.put("body", "");

    mockMvc
        .perform(
            post("/api/articles/{articleId}/comments", ARTICLE_ID)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
