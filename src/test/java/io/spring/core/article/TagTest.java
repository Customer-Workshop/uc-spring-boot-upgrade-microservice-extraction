package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  public void should_equal_by_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_not_equal_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1, tag2);
  }
}
