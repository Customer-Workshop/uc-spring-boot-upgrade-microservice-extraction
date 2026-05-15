package io.spring.application.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  public void should_create_with_defaults() {
    UpdateArticleParam param = new UpdateArticleParam();
    assertEquals("", param.getTitle());
    assertEquals("", param.getBody());
    assertEquals("", param.getDescription());
  }

  @Test
  public void should_create_with_all_args() {
    UpdateArticleParam param = new UpdateArticleParam("Title", "Body", "Desc");
    assertEquals("Title", param.getTitle());
    assertEquals("Body", param.getBody());
    assertEquals("Desc", param.getDescription());
  }
}
