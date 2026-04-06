package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.CommentQueryService;
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.infrastructure.service.CommentServiceClient;
import io.spring.infrastructure.service.CommentServiceClient.CommentResponse;
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
public class CommentsApiTest extends TestWithCurrentUser {

  @MockBean private ArticleRepository articleRepository;

  @MockBean private CommentServiceClient commentServiceClient;
  @MockBean private CommentQueryService commentQueryService;

  private Article article;
  private CommentData commentData;
  private CommentResponse commentResponse;
  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    super.setUp();
    article = new Article("title", "desc", "body", Arrays.asList("test", "java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));

    commentResponse = new CommentResponse();
    commentResponse.setId("comment-id");
    commentResponse.setBody("comment");
    commentResponse.setArticleId(article.getId());
    commentResponse.setUserId(user.getId());
    commentResponse.setCreatedAt("2026-01-01T00:00:00.000Z");
    commentResponse.setUpdatedAt("2026-01-01T00:00:00.000Z");

    commentData =
        new CommentData(
            commentResponse.getId(),
            commentResponse.getBody(),
            article.getId(),
            new org.joda.time.DateTime(),
            new org.joda.time.DateTime(),
            new ProfileData(
                user.getId(), user.getUsername(), user.getBio(), user.getImage(), false));
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

    when(commentServiceClient.createComment(anyString(), anyString(), anyString()))
        .thenReturn(commentResponse);
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
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(422)
        .body("errors.body[0]", equalTo("can't be empty"));
  }

  @Test
  public void should_get_comments_of_article_success() throws Exception {
    when(commentQueryService.findByArticleId(anyString(), eq(null)))
        .thenReturn(Arrays.asList(commentData));
    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentData.getId()));
  }

  @Test
  public void should_delete_comment_success() throws Exception {
    when(commentServiceClient.getCommentByIdAndArticleId(
            eq(commentResponse.getId()), eq(article.getId())))
        .thenReturn(Optional.of(commentResponse));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), commentResponse.getId())
        .then()
        .statusCode(204);
  }

  @Test
  public void should_get_403_if_not_author_of_article_or_author_of_comment_when_delete_comment()
      throws Exception {
    User anotherUser = new User("other@example.com", "other", "123", "", "");
    when(userRepository.findByUsername(eq(anotherUser.getUsername())))
        .thenReturn(Optional.of(anotherUser));
    when(jwtService.getSubFromToken(any())).thenReturn(Optional.of(anotherUser.getId()));
    when(userRepository.findById(eq(anotherUser.getId())))
        .thenReturn(Optional.ofNullable(anotherUser));

    when(commentServiceClient.getCommentByIdAndArticleId(
            eq(commentResponse.getId()), eq(article.getId())))
        .thenReturn(Optional.of(commentResponse));
    String token = jwtService.toToken(anotherUser);
    when(userRepository.findById(eq(anotherUser.getId()))).thenReturn(Optional.of(anotherUser));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), commentResponse.getId())
        .then()
        .statusCode(403);
  }
}
