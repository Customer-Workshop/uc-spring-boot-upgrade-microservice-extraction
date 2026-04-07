package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ArticleParamTest {

  @Test
  void should_create_new_article_param_with_builder() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Test Title")
            .description("Test Description")
            .body("Test Body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertEquals("Test Title", param.getTitle());
    assertEquals("Test Description", param.getDescription());
    assertEquals("Test Body", param.getBody());
    assertEquals(2, param.getTagList().size());
    assertTrue(param.getTagList().contains("java"));
    assertTrue(param.getTagList().contains("spring"));
  }

  @Test
  void should_create_new_article_param_with_no_arg_constructor() {
    NewArticleParam param = new NewArticleParam();

    assertNull(param.getTitle());
    assertNull(param.getDescription());
    assertNull(param.getBody());
    assertNull(param.getTagList());
  }

  @Test
  void should_create_new_article_param_with_all_args() {
    NewArticleParam param =
        new NewArticleParam("Title", "Desc", "Body", Arrays.asList("tag1"));

    assertEquals("Title", param.getTitle());
    assertEquals("Desc", param.getDescription());
    assertEquals("Body", param.getBody());
    assertEquals(1, param.getTagList().size());
  }

  @Test
  void should_create_update_article_param() {
    UpdateArticleParam param = new UpdateArticleParam("Title", "Body", "Desc");

    assertEquals("Title", param.getTitle());
    assertEquals("Body", param.getBody());
    assertEquals("Desc", param.getDescription());
  }

  @Test
  void should_create_update_article_param_with_no_args() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }

  @Test
  void should_create_new_article_param_with_empty_tags() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("Title")
            .description("Desc")
            .body("Body")
            .tagList(Collections.emptyList())
            .build();

    assertNotNull(param.getTagList());
    assertTrue(param.getTagList().isEmpty());
  }
}
