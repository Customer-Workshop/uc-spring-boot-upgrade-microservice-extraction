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
import io.spring.application.data.TagData;
import io.spring.core.article.Tag;
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
  public void should_get_tag_details() {
    TagData tag1 = new TagData("id1", "java", new DateTime());
    TagData tag2 = new TagData("id2", "spring", new DateTime());
    when(tagsQueryService.findAll()).thenReturn(Arrays.asList(tag1, tag2));

    given()
        .contentType("application/json")
        .when()
        .get("/tags/details")
        .then()
        .statusCode(200)
        .body("tags[0].name", equalTo("java"))
        .body("tags[1].name", equalTo("spring"));
  }

  @Test
  public void should_get_tag_by_id() {
    TagData tagData = new TagData("tag-1", "java", new DateTime());
    when(tagsQueryService.findById(eq("tag-1"))).thenReturn(Optional.of(tagData));

    given()
        .contentType("application/json")
        .when()
        .get("/tags/tag-1")
        .then()
        .statusCode(200)
        .body("tag.id", equalTo("tag-1"))
        .body("tag.name", equalTo("java"));
  }

  @Test
  public void should_return_404_for_nonexistent_tag() {
    when(tagsQueryService.findById(eq("nonexistent"))).thenReturn(Optional.empty());

    given().contentType("application/json").when().get("/tags/nonexistent").then().statusCode(404);
  }

  @Test
  public void should_create_tag() {
    Tag tag = new Tag("new-tag");
    when(tagsQueryService.createTag(eq("new-tag"))).thenReturn(tag);

    Map<String, Object> param = new HashMap<>();
    Map<String, Object> tagParam = new HashMap<>();
    tagParam.put("name", "new-tag");
    param.put("tag", tagParam);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .post("/tags")
        .then()
        .statusCode(201)
        .body("tag.name", equalTo("new-tag"));
  }

  @Test
  public void should_update_tag() {
    Tag tag = new Tag("updated-tag");
    when(tagsQueryService.updateTag(eq("tag-1"), eq("updated-tag"))).thenReturn(tag);

    Map<String, Object> param = new HashMap<>();
    Map<String, Object> tagParam = new HashMap<>();
    tagParam.put("name", "updated-tag");
    param.put("tag", tagParam);

    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .body(param)
        .when()
        .put("/tags/tag-1")
        .then()
        .statusCode(200)
        .body("tag.name", equalTo("updated-tag"));
  }

  @Test
  public void should_delete_tag() {
    given()
        .contentType("application/json")
        .header("Authorization", "Token " + token)
        .when()
        .delete("/tags/tag-1")
        .then()
        .statusCode(204);

    verify(tagsQueryService).removeTag(eq("tag-1"));
  }

  @Test
  public void should_require_auth_for_create() {
    Map<String, Object> param = new HashMap<>();
    Map<String, Object> tagParam = new HashMap<>();
    tagParam.put("name", "new-tag");
    param.put("tag", tagParam);

    given().contentType("application/json").body(param).when().post("/tags").then().statusCode(401);
  }

  @Test
  public void should_require_auth_for_delete() {
    given().contentType("application/json").when().delete("/tags/tag-1").then().statusCode(401);
  }
}
