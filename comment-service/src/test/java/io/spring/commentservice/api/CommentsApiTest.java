package io.spring.commentservice.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.commentservice.JacksonCustomizations;
import io.spring.commentservice.api.security.WebSecurityConfig;
import io.spring.commentservice.application.CommentQueryService;
import io.spring.commentservice.application.data.CommentData;
import io.spring.commentservice.application.data.ProfileData;
import io.spring.commentservice.client.ArticleServiceClient;
import io.spring.commentservice.core.Comment;
import io.spring.commentservice.core.CommentRepository;
import io.spring.commentservice.infrastructure.service.DefaultJwtService;
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

@WebMvcTest(CommentsApi.class)
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class CommentsApiTest {

  @MockBean private CommentRepository commentRepository;
  @MockBean private CommentQueryService commentQueryService;
  @MockBean private ArticleServiceClient articleServiceClient;
  @MockBean private DefaultJwtService jwtService;

  private String articleId;
  private String slug;
  private CommentData commentData;
  private Comment comment;
  private String userId;
  private String token;
  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    userId = "user-id-123";
    token = "test-token";
    slug = "title";
    articleId = "article-id-456";

    when(jwtService.getSubFromToken(eq(token))).thenReturn(Optional.of(userId));
    when(articleServiceClient.getArticleIdBySlug(eq(slug))).thenReturn(Optional.of(articleId));

    comment = new Comment("comment", userId, articleId);
    commentData =
        new CommentData(
            comment.getId(),
            comment.getBody(),
            comment.getArticleId(),
            comment.getCreatedAt(),
            comment.getCreatedAt(),
            new ProfileData(userId, "johnjacob", "", "", false));
  }

  @Test
  public void should_create_comment_success() throws Exception {
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

    when(commentQueryService.findById(anyString(), eq(userId)))
        .thenReturn(Optional.of(commentData));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", slug)
        .then()
        .statusCode(201)
        .body("comment.body", equalTo(commentData.getBody()));
  }

  @Test
  public void should_get_422_with_empty_body() throws Exception {
    Map<String, Object> param =
        new HashMap<String, Object>() {
          {
            put(
                "comment",
                new HashMap<String, Object>() {
                  {
                    put("body", "");
                  }
                });
          }
        };

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", slug)
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_comments_of_article_success() throws Exception {
    when(commentQueryService.findByArticleId(eq(articleId), eq(null)))
        .thenReturn(Arrays.asList(commentData));
    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", slug)
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentData.getId()));
  }

  @Test
  public void should_delete_comment_success() throws Exception {
    when(commentRepository.findById(eq(articleId), eq(comment.getId())))
        .thenReturn(Optional.of(comment));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", slug, comment.getId())
        .then()
        .statusCode(204);
  }

  @Test
  public void should_get_403_if_not_author_of_comment_when_delete_comment() throws Exception {
    String anotherUserId = "another-user-id";
    when(jwtService.getSubFromToken(eq("another-token")))
        .thenReturn(Optional.of(anotherUserId));

    Comment otherComment = new Comment("comment", userId, articleId);
    when(commentRepository.findById(eq(articleId), eq(otherComment.getId())))
        .thenReturn(Optional.of(otherComment));

    given()
        .header("Authorization", "Token another-token")
        .when()
        .delete("/articles/{slug}/comments/{id}", slug, otherComment.getId())
        .then()
        .statusCode(403);
  }
}
