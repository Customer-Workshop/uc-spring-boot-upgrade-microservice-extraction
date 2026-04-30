package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  public void should_create_with_all_args() {
    UpdateArticleParam param = new UpdateArticleParam("title", "body", "desc");
    assertEquals("title", param.getTitle());
    assertEquals("body", param.getBody());
    assertEquals("desc", param.getDescription());
  }

  @Test
  public void should_create_with_no_args_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertNotNull(param);
    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }
}
