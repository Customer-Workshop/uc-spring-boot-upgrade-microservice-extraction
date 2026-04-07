package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.comment.Comment;
import io.spring.core.comment.CommentRepository;
import io.spring.core.user.User;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
 * Integration tests that verify the monolith's CommentsApi correctly delegates to the comments
 * microservice via the CommentRepository (implemented by CommentServiceClient). These tests use
 * mocks to simulate the HTTP communication layer, verifying the contract between the monolith and
 * the comments microservice.
 */
@WebMvcTest(CommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentsIntegrationTest extends TestWithCurrentUser {

  @MockBean private ArticleRepository articleRepository;
  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;

  private Article article;
  private CommentData commentData;
  private Comment comment;
  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    super.setUp();
    article = new Article("title", "desc", "body", Arrays.asList("test", "java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    comment = new Comment("comment body", user.getId(), article.getId());
    commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
  }

  @Test
  public void should_create_comment_via_microservice() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "comment",
                new HashMap<String, Object>() {
                  {
                    put("body", "comment content");
                  }
                });
          }
        };

    when(commentQueryService.findById(anyString(), eq(user))).thenReturn(Optional.of(commentData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(201)
        .body("comment.body", equalTo(commentData.getBody()));

    // Verify that save was called on the CommentRepository (which delegates to HTTP client)
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  public void should_get_comments_from_microservice() throws Exception {
    when(commentQueryService.findByArticleId(anyString(), eq(null)))
        .thenReturn(Arrays.asList(commentData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentData.getId()))
        .body("comments[0].body", equalTo(commentData.getBody()));
  }

  @Test
  public void should_delete_comment_via_microservice() throws Exception {
    when(commentRepository.findById(eq(article.getId()), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), comment.getId())
        .then()
        .statusCode(204);

    // Verify that remove was called on the CommentRepository (which delegates to HTTP client)
    verify(commentRepository).remove(any(Comment.class));
  }

  @Test
  public void should_return_comment_with_author_profile() throws Exception {
    when(commentQueryService.findByArticleId(anyString(), eq(null)))
        .thenReturn(Arrays.asList(commentData));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(200)
        .body("comments[0].author.username", equalTo(user.getUsername()));
  }

  @Test
  public void should_return_multiple_comments_from_microservice() throws Exception {
    Comment comment2 = new Comment("second comment", user.getId(), article.getId());
    CommentData commentData2 =
        new CommentData(
            comment2.getId(),
            comment2.getBody(),
            comment2.getArticleId(),
            comment2.getCreatedAt(),
            comment2.getCreatedAt(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));

    when(commentQueryService.findByArticleId(anyString(), eq(null)))
        .thenReturn(Arrays.asList(commentData, commentData2));

    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentData.getId()))
        .body("comments[1].id", equalTo(commentData2.getId()));
  }
}
