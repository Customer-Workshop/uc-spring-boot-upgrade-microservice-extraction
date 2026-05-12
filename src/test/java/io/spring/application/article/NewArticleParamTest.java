package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  void should_create_with_builder() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("desc")
            .body("body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertEquals("Test Title", param.getTitle());
    assertEquals("desc", param.getDescription());
    assertEquals("body", param.getBody());
    assertEquals(2, param.getTagList().size());
  }

  @Test
  void should_create_with_no_args() {
    NewArticleParam param = new NewArticleParam();
    assertNull(param.getTitle());
    assertNull(param.getDescription());
    assertNull(param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  void should_create_with_all_args() {
    NewArticleParam param = new NewArticleParam("title", "desc", "body", Arrays.asList("tag"));
    assertEquals("title", param.getTitle());
    assertEquals("desc", param.getDescription());
    assertEquals("body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }
}
