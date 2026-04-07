package io.spring.integration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.JacksonCustomizations;
import io.spring.api.CommentsApi;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Integration test verifying the monolith's CommentsApi communicates correctly with the comments
 * microservice via the CommentRepository (backed by CommentsServiceClient in production).
 */
@WebMvcTest(CommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentsServiceIntegrationTest {

  @Autowired private MockMvc mvc;

  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;
  @MockBean private UserRepository userRepository;
  @MockBean private JwtService jwtService;

  private User user;
  private String token;
  private Article article;

  @BeforeEach
  public void setUp() {
    user = new User("integration@test.com", "integrationuser", "123", "bio", "image");
    token = "test-token";

    when(jwtService.getSubFromToken(eq("test-token"))).thenReturn(Optional.of(user.getId()));
    when(jwtService.toToken(eq(user))).thenReturn("test-token");
    when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

    article =
        new Article("test-article", "desc", "body", Arrays.asList("java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
  }

  @Test
  public void should_create_comment_via_service() throws Exception {
    Comment comment = new Comment("Integration test comment", user.getId(), article.getId());
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            profileData);

    when(commentQueryService.findById(anyString(), eq(user)))
        .thenReturn(Optional.of(commentData));

    Map<String, Object> param = new HashMap<>();
    Map<String, Object> commentParam = new HashMap<>();
    commentParam.put("body", "Integration test comment");
    param.put("comment", commentParam);

    mvc.perform(
            post("/articles/{slug}/comments", article.getSlug())
                .contentType("application/json")
                .header("Authorization", "Token " + token)
                .content(new ObjectMapper().writeValueAsString(param)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.body").value("Integration test comment"))
        .andExpect(jsonPath("$.comment.author.username").value("integrationuser"));
  }

  @Test
  public void should_get_comments_for_article_via_service() throws Exception {
    ProfileData profileData =
        new ProfileData(user.getId(), user.getUsername(), user.getBio(), user.getImage(), false);
    CommentData commentData1 =
        new CommentData(
            "c1", "Comment 1", article.getId(),
            new org.joda.time.DateTime(), new org.joda.time.DateTime(), profileData);
    CommentData commentData2 =
        new CommentData(
            "c2", "Comment 2", article.getId(),
            new org.joda.time.DateTime(), new org.joda.time.DateTime(), profileData);

    when(commentQueryService.findByArticleId(eq(article.getId()), eq(null)))
        .thenReturn(Arrays.asList(commentData1, commentData2));

    mvc.perform(get("/articles/{slug}/comments", article.getSlug()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments.length()").value(2))
        .andExpect(jsonPath("$.comments[0].body").value("Comment 1"))
        .andExpect(jsonPath("$.comments[1].body").value("Comment 2"));
  }

  @Test
  public void should_delete_comment_via_service() throws Exception {
    Comment comment = new Comment("To delete", user.getId(), article.getId());
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                    "/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
                .header("Authorization", "Token " + token))
        .andExpect(status().isNoContent());
  }
}
