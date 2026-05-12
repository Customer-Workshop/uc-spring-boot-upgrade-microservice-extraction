package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  void should_create_param_with_all_args() {
    UpdateArticleParam param = new UpdateArticleParam("New Title", "New Body", "New Desc");

    assertEquals("New Title", param.getTitle());
    assertEquals("New Body", param.getBody());
    assertEquals("New Desc", param.getDescription());
  }

  @Test
  void should_create_param_with_no_args_using_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }

  @Test
  void should_allow_partial_update() {
    UpdateArticleParam param = new UpdateArticleParam("Updated Title", "", "");

    assertEquals("Updated Title", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
