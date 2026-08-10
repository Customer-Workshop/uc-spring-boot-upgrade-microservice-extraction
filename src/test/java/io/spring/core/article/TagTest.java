package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_set_name_and_generate_id() {
    Tag tag = new Tag("java");
    assertThat(tag.getName(), is("java"));
    assertThat(tag.getId(), notNullValue());
  }

  @Test
  public void should_be_equal_by_name() {
    Tag tag = new Tag("java");
    assertThat(tag.equals(new Tag("java")), is(true));
    assertThat(tag.equals(new Tag("spring")), is(false));
    assertThat(tag.getId().equals(new Tag("java").getId()), is(false));
  }
}
