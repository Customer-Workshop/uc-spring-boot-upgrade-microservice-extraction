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
import io.spring.application.data.CommentData;
import io.spring.application.data.ProfileData;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.user.User;
import io.spring.infrastructure.service.comments.CommentServiceClient;
import io.spring.infrastructure.service.comments.CommentServiceResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
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

  private Article article;
  private CommentData commentData;
  private CommentServiceResponse commentServiceResponse;
  @Autowired private MockMvc mvc;

  @BeforeEach
  public void setUp() throws Exception {
    RestAssuredMockMvc.mockMvc(mvc);
    super.setUp();
    article = new Article("title", "desc", "body", Arrays.asList("test", "java"), user.getId());
    when(articleRepository.findBySlug(eq(article.getSlug()))).thenReturn(Optional.of(article));
    commentServiceResponse = new CommentServiceResponse();
    commentServiceResponse.setId("comment-id-1");
    commentServiceResponse.setBody("comment");
    commentServiceResponse.setArticleId(article.getId());
    commentServiceResponse.setUserId(user.getId());
    commentServiceResponse.setCreatedAt(new DateTime().toString());
    commentServiceResponse.setUpdatedAt(new DateTime().toString());
    commentData =
        new CommentData(
            commentServiceResponse.getId(),
            commentServiceResponse.getBody(),
            commentServiceResponse.getArticleId(),
            new DateTime(),
            new DateTime(),
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

    when(commentServiceClient.createComment(eq(article.getId()), anyString(), eq(user.getId())))
        .thenReturn(commentServiceResponse);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(201)
        .body("comment.body", equalTo(commentServiceResponse.getBody()));
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
    when(commentServiceClient.getCommentsByArticleId(anyString()))
        .thenReturn(Arrays.asList(commentServiceResponse));
    RestAssuredMockMvc.when()
        .get("/articles/{slug}/comments", article.getSlug())
        .prettyPeek()
        .then()
        .statusCode(200)
        .body("comments[0].id", equalTo(commentServiceResponse.getId()));
  }

  @Test
  public void should_delete_comment_success() throws Exception {
    when(commentServiceClient.getComment(eq(article.getId()), eq(commentServiceResponse.getId())))
        .thenReturn(Optional.of(commentServiceResponse));

    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete(
            "/articles/{slug}/comments/{id}",
            article.getSlug(),
            commentServiceResponse.getId())
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

    CommentServiceResponse otherComment = new CommentServiceResponse();
    otherComment.setId("other-comment-id");
    otherComment.setBody("other comment");
    otherComment.setArticleId(article.getId());
    otherComment.setUserId(user.getId());
    otherComment.setCreatedAt(new DateTime().toString());
    otherComment.setUpdatedAt(new DateTime().toString());

    when(commentServiceClient.getComment(eq(article.getId()), eq(otherComment.getId())))
        .thenReturn(Optional.of(otherComment));
    String token = jwtService.toToken(anotherUser);
    when(userRepository.findById(eq(anotherUser.getId()))).thenReturn(Optional.of(anotherUser));
    given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), otherComment.getId())
        .then()
        .statusCode(403);
  }
}
