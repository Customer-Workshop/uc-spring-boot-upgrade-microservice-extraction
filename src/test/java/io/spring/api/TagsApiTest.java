package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Tag;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({TagsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TagsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private TagsQueryService tagsQueryService;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_all_tags() throws Exception {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring", "kotlin"));

    given()
        .contentType("application/json")
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags.size()", equalTo(3));
  }

  @Test
  public void should_get_all_tag_entities() throws Exception {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    when(tagsQueryService.allTagEntities()).thenReturn(Arrays.asList(tag1, tag2));

    given()
        .contentType("application/json")
        .when()
        .get("/tags/entities")
        .then()
        .statusCode(200)
        .body("tags.size()", equalTo(2));
  }

  @Test
  public void should_get_tag_by_id() throws Exception {
    Tag tag = new Tag("java");
    when(tagsQueryService.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));

    given()
        .contentType("application/json")
        .when()
        .get("/tags/{id}", tag.getId())
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("java"));
  }

  @Test
  public void should_return_404_when_tag_not_found() throws Exception {
    when(tagsQueryService.findById(eq("non-existent"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .when()
        .get("/tags/{id}", "non-existent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_create_tag() throws Exception {
    Tag tag = new Tag("new-tag");
    when(tagsQueryService.createTag(eq("new-tag"))).thenReturn(tag);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\": {\"name\": \"new-tag\"}}")
        .when()
        .post("/tags")
        .then()
        .statusCode(201)
        .body("tag.name", equalTo("new-tag"));
  }

  @Test
  public void should_update_tag() throws Exception {
    Tag tag = new Tag("updated-tag");
    when(tagsQueryService.updateTag(eq(tag.getId()), eq("updated-tag"))).thenReturn(tag);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\": {\"name\": \"updated-tag\"}}")
        .when()
        .put("/tags/{id}", tag.getId())
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("updated-tag"));
  }

  @Test
  public void should_return_404_when_updating_nonexistent_tag() throws Exception {
    when(tagsQueryService.updateTag(eq("non-existent"), eq("new-name")))
        .thenThrow(new RuntimeException("Tag not found"));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\": {\"name\": \"new-name\"}}")
        .when()
        .put("/tags/{id}", "non-existent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_delete_tag() throws Exception {
    Tag tag = new Tag("delete-me");
    when(tagsQueryService.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/{id}", tag.getId())
        .then()
        .statusCode(204);

    verify(tagsQueryService).deleteTag(eq(tag.getId()));
  }

  @Test
  public void should_return_404_when_deleting_nonexistent_tag() throws Exception {
    when(tagsQueryService.findById(eq("non-existent"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/{id}", "non-existent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_reject_empty_tag_name() throws Exception {
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body("{\"tag\": {\"name\": \"\"}}")
        .when()
        .post("/tags")
        .then()
        .statusCode(422);
  }
}
