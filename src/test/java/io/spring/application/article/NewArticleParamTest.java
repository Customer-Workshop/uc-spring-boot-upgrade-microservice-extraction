package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  void should_create_with_builder() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(tags)
            .build();

    assertEquals("Test Title", param.getTitle());
    assertEquals("Test Description", param.getDescription());
    assertEquals("Test Body", param.getBody());
    assertEquals(2, param.getTagList().size());
    assertTrue(param.getTagList().contains("java"));
  }

  @Test
  void should_create_with_no_args_constructor() {
    NewArticleParam param = new NewArticleParam();

    assertNull(param.getTitle());
    assertNull(param.getDescription());
    assertNull(param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  void should_create_with_all_args_constructor() {
    List<String> tags = Collections.singletonList("tag1");
    NewArticleParam param = new NewArticleParam("Title", "Desc", "Body", tags);

    assertEquals("Title", param.getTitle());
    assertEquals("Desc", param.getDescription());
    assertEquals("Body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  void should_create_with_null_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(null)
            .build();

    assertNull(param.getTagList());
  }
}
