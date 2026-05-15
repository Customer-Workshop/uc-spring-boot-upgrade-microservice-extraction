package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  void should_create_tag_with_name() {
    Tag tag = new Tag("java");

    assertNotNull(tag.getId());
    assertEquals("java", tag.getName());
  }

  @Test
  void should_generate_unique_ids() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("kotlin");

    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  void should_be_equal_when_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");

    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  void should_not_be_equal_when_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("kotlin");

    assertNotEquals(tag1, tag2);
  }

  @Test
  void should_set_and_get_name() {
    Tag tag = new Tag("java");
    tag.setName("kotlin");

    assertEquals("kotlin", tag.getName());
  }
}
