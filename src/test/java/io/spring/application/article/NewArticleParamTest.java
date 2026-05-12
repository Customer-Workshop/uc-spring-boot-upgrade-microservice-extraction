package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class NewArticleParamTest {

  @Test
  void should_create_param_with_all_fields() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Description")
            .body("Body content")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertEquals("Test Title", param.getTitle());
    assertEquals("Description", param.getDescription());
    assertEquals("Body content", param.getBody());
    assertEquals(2, param.getTagList().size());
    assertTrue(param.getTagList().contains("java"));
  }

  @Test
  void should_create_param_with_no_args_constructor() {
    NewArticleParam param = new NewArticleParam();
    assertNull(param.getTitle());
    assertNull(param.getDescription());
    assertNull(param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  void should_create_param_with_all_args_constructor() {
    NewArticleParam param =
        new NewArticleParam("Title", "Desc", "Body", Collections.singletonList("tag1"));
    assertEquals("Title", param.getTitle());
    assertEquals("Desc", param.getDescription());
    assertEquals("Body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }
}
