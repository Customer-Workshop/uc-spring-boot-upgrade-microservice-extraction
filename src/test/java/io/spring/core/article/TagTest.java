package io.spring.core.article;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_generate_id_and_keep_name() {
    Tag tag = new Tag("java");

    assertThat(tag.getId(), notNullValue());
    assertThat(tag.getName(), is("java"));
    assertThat(tag.getId(), not(is(new Tag("java").getId())));
  }

  @Test
  public void should_only_be_equal_to_tag_with_same_name() {
    Tag tag = new Tag("java");

    assertThat(tag.equals(tag), is(true));
    assertThat(tag.equals(new Tag("java")), is(true));
    assertThat(tag.equals(new Tag("spring")), is(false));
    assertThat(tag.equals(null), is(false));
    assertThat(tag.equals("java"), is(false));
    assertThat(tag.equals(new Tag()), is(false));
    assertThat(new Tag().equals(tag), is(false));
    assertThat(new Tag().equals(new Tag()), is(true));
  }

  @Test
  public void should_have_hash_code_consistent_with_name() {
    Tag tag = new Tag("java");

    assertThat(tag.hashCode(), is(new Tag("java").hashCode()));
    assertThat(tag.hashCode(), not(is(new Tag("spring").hashCode())));
    assertThat(tag.hashCode(), not(is(0)));
    assertThat(new Tag().hashCode(), notNullValue());
  }

  @Test
  public void should_render_name_in_to_string() {
    assertThat(new Tag("java").toString(), containsString("java"));
  }
}
