package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  public void should_create_with_builder() {
    List<String> tags = Arrays.asList("java", "spring");
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Desc")
            .body("Test Body")
            .tagList(tags)
            .build();

    assertEquals("Test Title", param.getTitle());
    assertEquals("Test Desc", param.getDescription());
    assertEquals("Test Body", param.getBody());
    assertEquals(2, param.getTagList().size());
  }

  @Test
  public void should_create_with_all_args_constructor() {
    List<String> tags = Arrays.asList("java");
    NewArticleParam param = new NewArticleParam("title", "desc", "body", tags);

    assertEquals("title", param.getTitle());
    assertEquals("desc", param.getDescription());
    assertEquals("body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  public void should_create_with_no_args_constructor() {
    NewArticleParam param = new NewArticleParam();
    assertNotNull(param);
    assertNull(param.getTitle());
  }
}
