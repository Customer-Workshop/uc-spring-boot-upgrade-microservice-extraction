package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UpdateArticleParamTest {

  @Test
  void should_create_param_with_all_args() {
    UpdateArticleParam param = new UpdateArticleParam("title", "body", "desc");
    assertEquals("title", param.getTitle());
    assertEquals("body", param.getBody());
    assertEquals("desc", param.getDescription());
  }

  @Test
  void should_create_param_with_no_args_and_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
