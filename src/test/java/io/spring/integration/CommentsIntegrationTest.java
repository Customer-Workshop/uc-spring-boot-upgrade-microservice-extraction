package io.spring.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"comments.service.url=http://localhost:18089"})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CommentsIntegrationTest {

  private static WireMockServer wireMockServer;

  @LocalServerPort private int port;

  @Autowired private UserRepository userRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private JwtService jwtService;

  private User user;
  private Article article;
  private String token;

  @BeforeAll
  static void startWireMock() {
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(18089));
    wireMockServer.start();
  }

  @AfterAll
  static void stopWireMock() {
    wireMockServer.stop();
  }

  @BeforeEach
  void setUp() {
    wireMockServer.resetAll();
    RestAssured.port = port;

    user = new User("integration@test.com", "integrationuser", "password123", "bio", "image");
    userRepository.save(user);
    token = jwtService.toToken(user);

    article =
        new Article(
            "integration-test-article", "desc", "body", Arrays.asList("java"), user.getId());
    articleRepository.save(article);
  }

  @Test
  public void should_create_comment_via_comments_service() {
    String commentId = "comment-123";

    wireMockServer.stubFor(
        post(urlPathEqualTo("/api/comments"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        String.format(
                            "{\"id\":\"%s\",\"body\":\"Great article!\",\"userId\":\"%s\","
                                + "\"articleId\":\"%s\",\"createdAt\":\"2024-01-01T00:00:00.000Z\"}",
                            commentId, user.getId(), article.getId()))));

    wireMockServer.stubFor(
        get(urlPathEqualTo("/api/comments/" + commentId))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        String.format(
                            "{\"id\":\"%s\",\"body\":\"Great article!\",\"userId\":\"%s\","
                                + "\"articleId\":\"%s\",\"createdAt\":\"2024-01-01T00:00:00.000Z\"}",
                            commentId, user.getId(), article.getId()))));

    RestAssured.given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"comment\":{\"body\":\"Great article!\"}}")
        .when()
        .post("/articles/{slug}/comments", article.getSlug())
        .then()
        .statusCode(201);

    wireMockServer.verify(postRequestedFor(urlPathEqualTo("/api/comments")));
  }

  @Test
  public void should_get_comments_via_comments_service() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/api/comments"))
            .withQueryParam("articleId", WireMock.equalTo(article.getId()))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        String.format(
                            "[{\"id\":\"c1\",\"body\":\"Comment 1\",\"userId\":\"%s\","
                                + "\"articleId\":\"%s\",\"createdAt\":\"2024-01-01T00:00:00.000Z\"}]",
                            user.getId(), article.getId()))));

    RestAssured.when().get("/articles/{slug}/comments", article.getSlug()).then().statusCode(200);

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/api/comments"))
            .withQueryParam("articleId", WireMock.equalTo(article.getId())));
  }

  @Test
  public void should_delete_comment_via_comments_service() {
    String commentId = "comment-to-delete";

    wireMockServer.stubFor(
        get(urlPathEqualTo("/api/comments/" + commentId))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        String.format(
                            "{\"id\":\"%s\",\"body\":\"To delete\",\"userId\":\"%s\","
                                + "\"articleId\":\"%s\",\"createdAt\":\"2024-01-01T00:00:00.000Z\"}",
                            commentId, user.getId(), article.getId()))));

    wireMockServer.stubFor(
        delete(urlPathEqualTo("/api/comments/" + commentId))
            .willReturn(aResponse().withStatus(204)));

    RestAssured.given()
        .header("Authorization", "Token " + token)
        .when()
        .delete("/articles/{slug}/comments/{id}", article.getSlug(), commentId)
        .then()
        .statusCode(204);

    wireMockServer.verify(deleteRequestedFor(urlPathEqualTo("/api/comments/" + commentId)));
  }
}
