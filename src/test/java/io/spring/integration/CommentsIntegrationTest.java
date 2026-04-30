package io.spring.integration;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.RealWorldApplication;
import io.spring.core.article.Article;
import io.spring.core.article.ArticleRepository;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = RealWorldApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentsIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private RestTemplate restTemplate;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private JwtService jwtService;
  @Autowired private ObjectMapper objectMapper;

  private MockRestServiceServer mockServer;
  private User user;
  private Article article;
  private String token;

  @BeforeEach
  public void setUp() {
    mockServer = MockRestServiceServer.createServer(restTemplate);

    String unique = UUID.randomUUID().toString().substring(0, 8);
    user = new User(unique + "@test.com", "user-" + unique, "123", "bio", "image");
    userRepository.save(user);
    token = jwtService.toToken(user);

    article =
        new Article("Integration Test " + unique, "desc", "body", Arrays.asList(), user.getId());
    articleRepository.save(article);
  }

  @Test
  public void should_create_comment_via_microservice() throws Exception {
    String commentId = "test-comment-id";
    String createResponseJson =
        "{\"id\":\""
            + commentId
            + "\",\"body\":\"test comment\",\"userId\":\""
            + user.getId()
            + "\",\"articleId\":\""
            + article.getId()
            + "\",\"createdAt\":\"2026-01-01T00:00:00.000Z\",\"updatedAt\":\"2026-01-01T00:00:00.000Z\"}";

    String getResponseJson = createResponseJson;

    mockServer
        .expect(ExpectedCount.once(), requestTo("http://localhost:8081/api/comments"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(createResponseJson, MediaType.APPLICATION_JSON));

    mockServer
        .expect(ExpectedCount.once(), requestTo("http://localhost:8081/api/comments/" + commentId))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(getResponseJson, MediaType.APPLICATION_JSON));

    Map<String, Object> param = new HashMap<>();
    Map<String, Object> commentParam = new HashMap<>();
    commentParam.put("body", "test comment");
    param.put("comment", commentParam);

    mockMvc
        .perform(
            post("/articles/{slug}/comments", article.getSlug())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Token " + token)
                .content(objectMapper.writeValueAsString(param)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.comment.id", notNullValue()))
        .andExpect(jsonPath("$.comment.body", equalTo("test comment")));

    mockServer.verify();
  }

  @Test
  public void should_get_comments_via_microservice() throws Exception {
    String commentsJson =
        "[{\"id\":\"c1\",\"body\":\"comment one\",\"userId\":\""
            + user.getId()
            + "\",\"articleId\":\""
            + article.getId()
            + "\",\"createdAt\":\"2026-01-01T00:00:00.000Z\",\"updatedAt\":\"2026-01-01T00:00:00.000Z\"},"
            + "{\"id\":\"c2\",\"body\":\"comment two\",\"userId\":\""
            + user.getId()
            + "\",\"articleId\":\""
            + article.getId()
            + "\",\"createdAt\":\"2026-01-02T00:00:00.000Z\",\"updatedAt\":\"2026-01-02T00:00:00.000Z\"}]";

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo("http://localhost:8081/api/comments?articleId=" + article.getId()))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(commentsJson, MediaType.APPLICATION_JSON));

    mockMvc
        .perform(get("/articles/{slug}/comments", article.getSlug()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.comments", hasSize(2)))
        .andExpect(jsonPath("$.comments[0].body", notNullValue()));

    mockServer.verify();
  }

  @Test
  public void should_delete_comment_via_microservice() throws Exception {
    String commentId = "delete-comment-id";
    String getResponseJson =
        "{\"id\":\""
            + commentId
            + "\",\"body\":\"to delete\",\"userId\":\""
            + user.getId()
            + "\",\"articleId\":\""
            + article.getId()
            + "\",\"createdAt\":\"2026-01-01T00:00:00.000Z\",\"updatedAt\":\"2026-01-01T00:00:00.000Z\"}";

    mockServer
        .expect(
            ExpectedCount.once(),
            requestTo(
                "http://localhost:8081/api/comments/"
                    + commentId
                    + "?articleId="
                    + article.getId()))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess(getResponseJson, MediaType.APPLICATION_JSON));

    mockServer
        .expect(ExpectedCount.once(), requestTo("http://localhost:8081/api/comments/" + commentId))
        .andExpect(method(HttpMethod.DELETE))
        .andRespond(withNoContent());

    mockMvc
        .perform(
            delete("/articles/{slug}/comments/{id}", article.getSlug(), commentId)
                .header("Authorization", "Token " + token))
        .andExpect(status().isNoContent());

    mockServer.verify();
  }
}
