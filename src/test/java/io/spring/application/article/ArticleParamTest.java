package io.spring.application.article;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class ArticleParamTest {

  @Test
  public void should_build_new_article_param_with_all_fields() {
    NewArticleParam param =
        NewArticleParam.builder()
            .title("a title")
            .description("a description")
            .body("a body")
            .tagList(Arrays.asList("java", "spring"))
            .build();

    assertThat(param, notNullValue());
    assertThat(param.getTitle(), is("a title"));
    assertThat(param.getDescription(), is("a description"));
    assertThat(param.getBody(), is("a body"));
    assertThat(param.getTagList(), is(Arrays.asList("java", "spring")));
  }

  @Test
  public void should_render_fields_in_new_article_param_builder_to_string() {
    String builderString =
        NewArticleParam.builder().title("a title").description("a description").toString();

    assertThat(builderString, containsString("a title"));
    assertThat(builderString, containsString("a description"));
  }

  @Test
  public void should_expose_update_article_param_fields() {
    UpdateArticleParam param = new UpdateArticleParam("a title", "a body", "a description");

    assertThat(param.getTitle(), is("a title"));
    assertThat(param.getBody(), is("a body"));
    assertThat(param.getDescription(), is("a description"));
  }

  @Test
  public void should_default_update_article_param_fields_to_empty_string() {
    UpdateArticleParam param = new UpdateArticleParam();

    assertThat(param.getTitle(), is(""));
    assertThat(param.getBody(), is(""));
    assertThat(param.getDescription(), is(""));
  }
}
