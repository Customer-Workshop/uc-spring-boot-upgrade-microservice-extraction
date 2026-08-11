package io.spring.core.article;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void tags_with_same_name_should_be_equal_with_same_hash_code() {
    Tag java = new Tag("java");
    Tag anotherJava = new Tag("java");
    assertEquals(java, anotherJava);
    assertEquals(java.hashCode(), anotherJava.hashCode());
  }

  @Test
  public void tags_with_different_names_should_not_be_equal() {
    Tag java = new Tag("java");
    Tag kotlin = new Tag("kotlin");
    assertNotEquals(java, kotlin);
    assertNotEquals(kotlin, java);
    assertNotEquals(java.hashCode(), kotlin.hashCode());
  }

  @Test
  public void tag_should_not_equal_null_name_tag_or_other_types() {
    Tag java = new Tag("java");
    Tag nullName = new Tag();
    assertFalse(java.equals(nullName));
    assertFalse(nullName.equals(java));
    assertTrue(nullName.equals(new Tag()));
    assertFalse(java.equals(null));
    assertFalse(java.equals("java"));
    assertNotEquals(java.hashCode(), nullName.hashCode());
  }

  @Test
  public void can_equal_should_reject_non_tag_types() {
    Tag tag = new Tag("java");
    assertFalse(tag.canEqual("java"));
    assertTrue(tag.canEqual(new Tag()));
  }

  @Test
  public void to_string_should_contain_name() {
    Tag tag = new Tag("java");
    assertThat(tag.toString(), containsString("java"));
    assertThat(tag.getName(), is("java"));
  }
}
