package io.spring.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import io.spring.application.CommentQueryService;
import io.spring.application.CursorPageParameter;
import io.spring.application.CursorPager;
import io.spring.application.data.CommentData;
import io.spring.core.comment.Comment;
import io.spring.infrastructure.mybatis.readservice.UserRelationshipQueryService;
import io.spring.infrastructure.service.CommentServiceClient;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Upgrade/migration tests that verify the monolith correctly delegates comment operations to the
 * comments microservice after extraction. These tests validate:
 *
 * <ul>
 *   <li>Comment ID consistency between monolith and microservice
 *   <li>Full CRUD lifecycle via HTTP delegation
 *   <li>Data mapping correctness during migration
 *   <li>Cursor-based pagination after migration
 *   <li>Backward compatibility of comment operations
 * </ul>
 */
@DisplayName("Upgrade/Migration Tests")
public class UpgradeMigrationTest {

  private MockRestServiceServer mockServer;
  private CommentServiceClient commentServiceClient;
  private CommentQueryService commentQueryService;
  private UserRelationshipQueryService userRelationshipQueryService;
  private final String commentsServiceUrl = "http://localhost:8081";

  @BeforeEach
  void setUp() {
    RestTemplate restTemplate = new RestTemplate();
    mockServer = MockRestServiceServer.createServer(restTemplate);
    commentServiceClient = new CommentServiceClient(restTemplate, commentsServiceUrl);
    userRelationshipQueryService = Mockito.mock(UserRelationshipQueryService.class);
    commentQueryService =
        new CommentQueryService(commentServiceClient, userRelationshipQueryService);
  }

  @Nested
  @DisplayName("Comment ID Consistency")
  class CommentIdConsistency {

    @Test
    @DisplayName("save() should send the monolith-generated comment ID to the microservice")
    void saveShouldSendCommentIdToMicroservice() {
      Comment comment = new Comment("Test body", "user-1", "article-1");
      String expectedId = comment.getId();

      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments"))
          .andExpect(method(HttpMethod.POST))
          .andExpect(content().json("{\"id\":\"" + expectedId + "\"}"))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + expectedId
                      + "\",\"body\":\"Test body\",\"userId\":\"user-1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      commentServiceClient.save(comment);
      mockServer.verify();
    }

    @Test
    @DisplayName("findById should preserve the original comment ID from microservice response")
    void findByIdShouldPreserveOriginalCommentId() {
      String originalId = "preserved-id-123";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + originalId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + originalId
                      + "\",\"body\":\"Test\",\"userId\":\"user-1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      Optional<Comment> result = commentServiceClient.findById("article-1", originalId);

      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(originalId);
      mockServer.verify();
    }

    @Test
    @DisplayName("delete should use the correct comment ID from findById result")
    void deleteShouldUseCorrectIdFromFindByIdResult() {
      String commentId = "comment-to-delete";

      // First: findById returns the comment with its original ID
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Test\",\"userId\":\"user-1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      // Second: delete uses the same ID
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.DELETE))
          .andRespond(withNoContent());

      Optional<Comment> found = commentServiceClient.findById("article-1", commentId);
      assertThat(found).isPresent();
      commentServiceClient.remove(found.get());

      mockServer.verify();
    }
  }

  @Nested
  @DisplayName("Full CRUD Lifecycle via HTTP Delegation")
  class CrudLifecycle {

    @Test
    @DisplayName("Create-Read-Delete lifecycle should work end-to-end via HTTP")
    void createReadDeleteLifecycleShouldWork() {
      Comment comment = new Comment("Lifecycle test", "user-1", "article-1");
      String commentId = comment.getId();

      // Step 1: Create
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Lifecycle test\",\"userId\":\"user-1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      // Step 2: Read by ID
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Lifecycle test\",\"userId\":\"user-1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      // Step 3: Delete
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.DELETE))
          .andRespond(withNoContent());

      // Execute lifecycle
      commentServiceClient.save(comment);
      Optional<Comment> found = commentServiceClient.findById("article-1", commentId);
      assertThat(found).isPresent();
      assertThat(found.get().getBody()).isEqualTo("Lifecycle test");
      commentServiceClient.remove(found.get());

      mockServer.verify();
    }

    @Test
    @DisplayName("Listing comments by article ID should return multiple comments")
    void listCommentsByArticleIdShouldReturnMultipleComments() {
      String articleId = "article-1";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "[{\"id\":\"c1\",\"body\":\"First\",\"userId\":\"u1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
                      + "{\"id\":\"c2\",\"body\":\"Second\",\"userId\":\"u2\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-02T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-02T00:00:00Z\"},"
                      + "{\"id\":\"c3\",\"body\":\"Third\",\"userId\":\"u3\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-01-03T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-03T00:00:00Z\"}]",
                  MediaType.APPLICATION_JSON));

      List<Map<String, Object>> comments = commentServiceClient.findByArticleId(articleId);

      assertThat(comments).hasSize(3);
      assertThat(comments.get(0).get("id")).isEqualTo("c1");
      assertThat(comments.get(1).get("id")).isEqualTo("c2");
      assertThat(comments.get(2).get("id")).isEqualTo("c3");
      mockServer.verify();
    }

    @Test
    @DisplayName("Empty article should return empty comment list")
    void emptyArticleShouldReturnEmptyCommentList() {
      String articleId = "empty-article";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

      List<Map<String, Object>> comments = commentServiceClient.findByArticleId(articleId);

      assertThat(comments).isEmpty();
      mockServer.verify();
    }
  }

  @Nested
  @DisplayName("Data Mapping Correctness After Migration")
  class DataMappingCorrectness {

    @Test
    @DisplayName("CommentQueryService should map microservice response to CommentData correctly")
    void commentQueryServiceShouldMapResponseToCommentData() {
      String commentId = "mapped-comment";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Mapped body\",\"userId\":\"user-1\","
                      + "\"articleId\":\"article-1\",\"createdAt\":\"2026-06-15T10:30:00Z\","
                      + "\"updatedAt\":\"2026-06-15T11:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      Optional<CommentData> result = commentQueryService.findById(commentId, null);

      assertThat(result).isPresent();
      CommentData data = result.get();
      assertThat(data.getId()).isEqualTo(commentId);
      assertThat(data.getBody()).isEqualTo("Mapped body");
      assertThat(data.getArticleId()).isEqualTo("article-1");
      assertThat(data.getCreatedAt()).isNotNull();
      assertThat(data.getUpdatedAt()).isNotNull();
      assertThat(data.getProfileData()).isNotNull();
      assertThat(data.getProfileData().getId()).isEqualTo("user-1");
      mockServer.verify();
    }

    @Test
    @DisplayName(
        "CommentQueryService should map multiple comments from microservice for article listing")
    void commentQueryServiceShouldMapMultipleCommentsForArticle() {
      String articleId = "article-mapped";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "[{\"id\":\"c1\",\"body\":\"Body 1\",\"userId\":\"u1\","
                      + "\"articleId\":\"article-mapped\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
                      + "{\"id\":\"c2\",\"body\":\"Body 2\",\"userId\":\"u2\","
                      + "\"articleId\":\"article-mapped\",\"createdAt\":\"2026-01-02T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-02T00:00:00Z\"}]",
                  MediaType.APPLICATION_JSON));

      List<CommentData> comments = commentQueryService.findByArticleId(articleId, null);

      assertThat(comments).hasSize(2);
      assertThat(comments.get(0).getId()).isEqualTo("c1");
      assertThat(comments.get(0).getBody()).isEqualTo("Body 1");
      assertThat(comments.get(1).getId()).isEqualTo("c2");
      assertThat(comments.get(1).getBody()).isEqualTo("Body 2");
      mockServer.verify();
    }

    @Test
    @DisplayName("Comment fields should preserve all data through HTTP serialization roundtrip")
    void commentFieldsShouldPreserveThroughRoundtrip() {
      Comment original = new Comment("Roundtrip body", "user-abc", "article-xyz");
      String commentId = original.getId();

      // Save
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Roundtrip body\","
                      + "\"userId\":\"user-abc\",\"articleId\":\"article-xyz\","
                      + "\"createdAt\":\"2026-03-20T12:00:00Z\","
                      + "\"updatedAt\":\"2026-03-20T12:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      // Retrieve
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Roundtrip body\","
                      + "\"userId\":\"user-abc\",\"articleId\":\"article-xyz\","
                      + "\"createdAt\":\"2026-03-20T12:00:00Z\","
                      + "\"updatedAt\":\"2026-03-20T12:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      commentServiceClient.save(original);
      Optional<Comment> retrieved = commentServiceClient.findById("article-xyz", commentId);

      assertThat(retrieved).isPresent();
      Comment result = retrieved.get();
      assertThat(result.getId()).isEqualTo(commentId);
      assertThat(result.getBody()).isEqualTo("Roundtrip body");
      assertThat(result.getUserId()).isEqualTo("user-abc");
      assertThat(result.getArticleId()).isEqualTo("article-xyz");
      mockServer.verify();
    }
  }

  @Nested
  @DisplayName("Cursor-Based Pagination After Migration")
  class CursorPaginationAfterMigration {

    @Test
    @DisplayName(
        "Cursor pagination should filter comments created before cursor for NEXT direction")
    void cursorPaginationShouldFilterForNextDirection() {
      String articleId = "article-paginated";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "[{\"id\":\"c1\",\"body\":\"Old\",\"userId\":\"u1\","
                      + "\"articleId\":\"article-paginated\","
                      + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
                      + "{\"id\":\"c2\",\"body\":\"Middle\",\"userId\":\"u2\","
                      + "\"articleId\":\"article-paginated\","
                      + "\"createdAt\":\"2026-06-01T00:00:00Z\",\"updatedAt\":\"2026-06-01T00:00:00Z\"},"
                      + "{\"id\":\"c3\",\"body\":\"Recent\",\"userId\":\"u3\","
                      + "\"articleId\":\"article-paginated\","
                      + "\"createdAt\":\"2026-12-01T00:00:00Z\",\"updatedAt\":\"2026-12-01T00:00:00Z\"}]",
                  MediaType.APPLICATION_JSON));

      // Cursor at middle comment; NEXT should return comments before cursor (older)
      DateTime cursor = DateTime.parse("2026-06-01T00:00:00Z");
      CursorPageParameter<DateTime> page =
          new CursorPageParameter<>(cursor, 10, CursorPager.Direction.NEXT);

      CursorPager<CommentData> result =
          commentQueryService.findByArticleIdWithCursor(articleId, null, page);

      assertThat(result.getData()).hasSize(1);
      assertThat(result.getData().get(0).getId()).isEqualTo("c1");
      mockServer.verify();
    }

    @Test
    @DisplayName("Cursor pagination should return all comments when no cursor is provided")
    void cursorPaginationShouldReturnAllWhenNoCursor() {
      String articleId = "article-no-cursor";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "[{\"id\":\"c1\",\"body\":\"First\",\"userId\":\"u1\","
                      + "\"articleId\":\"article-no-cursor\","
                      + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
                      + "{\"id\":\"c2\",\"body\":\"Second\",\"userId\":\"u2\","
                      + "\"articleId\":\"article-no-cursor\","
                      + "\"createdAt\":\"2026-02-01T00:00:00Z\",\"updatedAt\":\"2026-02-01T00:00:00Z\"}]",
                  MediaType.APPLICATION_JSON));

      CursorPageParameter<DateTime> page =
          new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

      CursorPager<CommentData> result =
          commentQueryService.findByArticleIdWithCursor(articleId, null, page);

      assertThat(result.getData()).hasSize(2);
      assertThat(result.hasNext()).isFalse();
      mockServer.verify();
    }

    @Test
    @DisplayName("Cursor pagination should indicate hasNext when more results exist than limit")
    void cursorPaginationShouldIndicateHasNext() {
      String articleId = "article-has-next";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "[{\"id\":\"c1\",\"body\":\"A\",\"userId\":\"u1\","
                      + "\"articleId\":\"article-has-next\","
                      + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"updatedAt\":\"2026-01-01T00:00:00Z\"},"
                      + "{\"id\":\"c2\",\"body\":\"B\",\"userId\":\"u2\","
                      + "\"articleId\":\"article-has-next\","
                      + "\"createdAt\":\"2026-02-01T00:00:00Z\",\"updatedAt\":\"2026-02-01T00:00:00Z\"},"
                      + "{\"id\":\"c3\",\"body\":\"C\",\"userId\":\"u3\","
                      + "\"articleId\":\"article-has-next\","
                      + "\"createdAt\":\"2026-03-01T00:00:00Z\",\"updatedAt\":\"2026-03-01T00:00:00Z\"}]",
                  MediaType.APPLICATION_JSON));

      // Limit to 2, so 3 results means hasNext=true
      CursorPageParameter<DateTime> page =
          new CursorPageParameter<>(null, 2, CursorPager.Direction.NEXT);

      CursorPager<CommentData> result =
          commentQueryService.findByArticleIdWithCursor(articleId, null, page);

      assertThat(result.getData()).hasSize(2);
      assertThat(result.hasNext()).isTrue();
      mockServer.verify();
    }

    @Test
    @DisplayName("Empty article should return empty cursor pager")
    void emptyArticleShouldReturnEmptyCursorPager() {
      String articleId = "empty-article";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments?articleId=" + articleId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

      CursorPageParameter<DateTime> page =
          new CursorPageParameter<>(null, 10, CursorPager.Direction.NEXT);

      CursorPager<CommentData> result =
          commentQueryService.findByArticleIdWithCursor(articleId, null, page);

      assertThat(result.getData()).isEmpty();
      assertThat(result.hasNext()).isFalse();
      mockServer.verify();
    }
  }

  @Nested
  @DisplayName("Backward Compatibility")
  class BackwardCompatibility {

    @Test
    @DisplayName("CommentServiceClient should implement CommentRepository interface")
    void commentServiceClientShouldImplementCommentRepository() {
      assertThat(commentServiceClient).isInstanceOf(io.spring.core.comment.CommentRepository.class);
    }

    @Test
    @DisplayName("save() should accept Comment objects created by the monolith")
    void saveShouldAcceptMonolithCreatedComments() {
      Comment comment = new Comment("body", "userId", "articleId");

      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + comment.getId()
                      + "\",\"body\":\"body\",\"userId\":\"userId\","
                      + "\"articleId\":\"articleId\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      commentServiceClient.save(comment);
      mockServer.verify();
    }

    @Test
    @DisplayName("findById with mismatched articleId should return empty (access control)")
    void findByIdWithMismatchedArticleIdShouldReturnEmpty() {
      String commentId = "c1";
      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\"c1\",\"body\":\"Test\",\"userId\":\"u1\","
                      + "\"articleId\":\"article-A\",\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      // Request with article-B but comment belongs to article-A
      Optional<Comment> result = commentServiceClient.findById("article-B", commentId);

      assertThat(result).isEmpty();
      mockServer.verify();
    }

    @Test
    @DisplayName(
        "Comment body, userId, and articleId should be preserved through save and retrieve")
    void commentFieldsShouldBePreservedThroughSaveAndRetrieve() {
      String body = "Special chars: <>&\"'";
      String userId = "user-with-special-id";
      String articleId = "article-with-special-id";
      Comment comment = new Comment(body, userId, articleId);
      String commentId = comment.getId();

      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments"))
          .andExpect(method(HttpMethod.POST))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Special chars: <>&\\\"'\","
                      + "\"userId\":\""
                      + userId
                      + "\",\"articleId\":\""
                      + articleId
                      + "\","
                      + "\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      mockServer
          .expect(requestTo(commentsServiceUrl + "/api/comments/" + commentId))
          .andExpect(method(HttpMethod.GET))
          .andRespond(
              withSuccess(
                  "{\"id\":\""
                      + commentId
                      + "\",\"body\":\"Special chars: <>&\\\"'\","
                      + "\"userId\":\""
                      + userId
                      + "\",\"articleId\":\""
                      + articleId
                      + "\","
                      + "\"createdAt\":\"2026-01-01T00:00:00Z\","
                      + "\"updatedAt\":\"2026-01-01T00:00:00Z\"}",
                  MediaType.APPLICATION_JSON));

      commentServiceClient.save(comment);
      Optional<Comment> retrieved = commentServiceClient.findById(articleId, commentId);

      assertThat(retrieved).isPresent();
      assertThat(retrieved.get().getBody()).isEqualTo(body);
      assertThat(retrieved.get().getUserId()).isEqualTo(userId);
      assertThat(retrieved.get().getArticleId()).isEqualTo(articleId);
      mockServer.verify();
    }
  }
}
