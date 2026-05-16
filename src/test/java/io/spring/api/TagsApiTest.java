package io.spring.api;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.spring.JacksonCustomizations;
import io.spring.api.security.WebSecurityConfig;
import io.spring.application.TagsQueryService;
import io.spring.core.article.Tag;
import io.spring.core.article.TagRepository;
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

@WebMvcTest({TagsApi.class})
@Import({WebSecurityConfig.class, JacksonCustomizations.class})
public class TagsApiTest extends TestWithCurrentUser {
  @Autowired private MockMvc mvc;

  @MockBean private TagsQueryService tagsQueryService;

  @MockBean private TagRepository tagRepository;

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
    RestAssuredMockMvc.mockMvc(mvc);
  }

  @Test
  public void should_get_all_tags() {
    when(tagsQueryService.allTags()).thenReturn(Arrays.asList("java", "spring"));

    given()
        .contentType("application/json")
        .when()
        .get("/tags")
        .then()
        .statusCode(200)
        .body("tags[0]", equalTo("java"))
        .body("tags[1]", equalTo("spring"));
  }

  @Test
  public void should_get_tag_by_id() {
    Tag tag = new Tag("java");
    when(tagRepository.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));

    given()
        .contentType("application/json")
        .when()
        .get("/tags/{id}", tag.getId())
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("java"));
  }

  @Test
  public void should_return_404_for_nonexistent_tag() {
    when(tagRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .when()
        .get("/tags/{id}", "nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_create_tag() {
    Map<String, Object> tagData = new HashMap<>();
    tagData.put("name", "new-tag");
    Map<String, Object> param = new HashMap<>();
    param.put("tag", tagData);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/tags")
        .then()
        .statusCode(201)
        .body("tag.name", equalTo("new-tag"));

    verify(tagRepository).save(any(Tag.class));
  }

  @Test
  public void should_update_tag() {
    Tag tag = new Tag("old-name");
    when(tagRepository.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));

    Map<String, Object> tagData = new HashMap<>();
    tagData.put("name", "new-name");
    Map<String, Object> param = new HashMap<>();
    param.put("tag", tagData);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/tags/{id}", tag.getId())
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("new-name"));

    verify(tagRepository).save(any(Tag.class));
  }

  @Test
  public void should_return_404_when_updating_nonexistent_tag() {
    when(tagRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    Map<String, Object> tagData = new HashMap<>();
    tagData.put("name", "new-name");
    Map<String, Object> param = new HashMap<>();
    param.put("tag", tagData);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/tags/{id}", "nonexistent")
        .then()
        .statusCode(404);
  }

  @Test
  public void should_delete_tag() {
    Tag tag = new Tag("to-delete");
    when(tagRepository.findById(eq(tag.getId()))).thenReturn(Optional.of(tag));

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/{id}", tag.getId())
        .then()
        .statusCode(204);

    verify(tagRepository).removeArticleTagsByTagId(eq(tag.getId()));
    verify(tagRepository).remove(eq(tag.getId()));
  }

  @Test
  public void should_return_404_when_deleting_nonexistent_tag() {
    when(tagRepository.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/{id}", "nonexistent")
        .then()
        .statusCode(404);
  }
}
