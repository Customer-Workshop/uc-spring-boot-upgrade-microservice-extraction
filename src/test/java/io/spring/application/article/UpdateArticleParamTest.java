package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  void should_create_with_all_args() {
    UpdateArticleParam param = new UpdateArticleParam("title", "body", "desc");
    assertEquals("title", param.getTitle());
    assertEquals("body", param.getBody());
    assertEquals("desc", param.getDescription());
  }

  @Test
  void should_create_with_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
