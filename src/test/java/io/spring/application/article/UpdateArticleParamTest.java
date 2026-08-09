package io.spring.application.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UpdateArticleParamTest {

  @Test
  public void should_default_all_fields_to_empty_strings() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertThat(param.getTitle(), is(""));
    assertThat(param.getDescription(), is(""));
    assertThat(param.getBody(), is(""));
  }

  @Test
  public void should_map_all_fields_through_constructor() {
    UpdateArticleParam param = new UpdateArticleParam("title", "body", "description");

    assertThat(param.getTitle(), is("title"));
    assertThat(param.getBody(), is("body"));
    assertThat(param.getDescription(), is("description"));
  }
}
