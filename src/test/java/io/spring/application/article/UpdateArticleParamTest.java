package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  void should_create_with_all_args() {
    UpdateArticleParam param = new UpdateArticleParam("Title", "Body", "Desc");
    assertEquals("Title", param.getTitle());
    assertEquals("Body", param.getBody());
    assertEquals("Desc", param.getDescription());
  }

  @Test
  void should_create_with_no_args() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }

  @Test
  void should_have_empty_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertNotNull(param.getTitle());
    assertNotNull(param.getBody());
    assertNotNull(param.getDescription());
  }
}
