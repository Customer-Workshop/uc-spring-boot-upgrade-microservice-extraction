package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class NewArticleParamTest {

  @Test
  void should_create_with_builder() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(Arrays.asList("java"))
            .build();

    assertEquals("Title", param.getTitle());
    assertEquals("Desc", param.getDescription());
    assertEquals("Body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  void should_create_with_all_args_constructor() {
    List<String> tags = Arrays.asList("spring", "boot");
    NewArticleParam param = new NewArticleParam("Title", "Desc", "Body", tags);

    assertEquals("Title", param.getTitle());
    assertEquals(2, param.getTagList().size());
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
  void should_handle_empty_tag_list() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("T")
            .description("D")
            .body("B")
            .tagList(Collections.emptyList())
            .build();
    assertTrue(param.getTagList().isEmpty());
  }

  @Test
  void should_handle_null_tag_list() {
    NewArticleParam param = NewArticleParam.builder().title("T").description("D").body("B").build();
    assertNull(param.getTagList());
  }
}
