package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NewArticleParamTest {

  @Test
  void should_create_param_with_builder() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test")
            .description("Desc")
            .body("Body")
            .build();

    assertEquals("Test", param.getTitle());
    assertEquals("Desc", param.getDescription());
    assertEquals("Body", param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  void should_create_param_with_all_args() {
    NewArticleParam param =
        new NewArticleParam("Title", "Desc", "Body", java.util.Arrays.asList("tag1"));

    assertEquals("Title", param.getTitle());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  void should_create_update_param() {
    UpdateArticleParam param = new UpdateArticleParam("Title", "Body", "Desc");

    assertEquals("Title", param.getTitle());
    assertEquals("Body", param.getBody());
    assertEquals("Desc", param.getDescription());
  }

  @Test
  void should_create_default_update_param() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
