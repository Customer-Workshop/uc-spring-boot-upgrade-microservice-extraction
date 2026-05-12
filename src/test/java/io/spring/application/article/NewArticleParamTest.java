package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  void should_create_param_with_builder() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("My Title")
            .description("My Description")
            .body("My Body")
            .tagList(tags)
            .build();

    assertEquals("My Title", param.getTitle());
    assertEquals("My Description", param.getDescription());
    assertEquals("My Body", param.getBody());
    assertEquals(tags, param.getTagList());
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
    List<String> tags = Arrays.asList("kotlin");
    NewArticleParam param = new NewArticleParam("Title", "Desc", "Body", tags);

    assertEquals("Title", param.getTitle());
    assertEquals("Desc", param.getDescription());
    assertEquals("Body", param.getBody());
    assertEquals(tags, param.getTagList());
  }
}
