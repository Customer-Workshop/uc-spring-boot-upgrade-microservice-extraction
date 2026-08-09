package io.spring.core.article;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_equal_tags_with_same_name_and_reject_different_names() {
    Tag java = new Tag("java");
    Tag anotherJava = new Tag("java");
    Tag spring = new Tag("spring");

    assertThat(java.equals(anotherJava), is(true));
    assertThat(java.equals(spring), is(false));
  }
}
