package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  public void should_generate_unique_ids() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1.getId(), tag2.getId());
  }

  @Test
  public void should_use_name_for_equality() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
  }

  @Test
  public void should_not_be_equal_with_different_names() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1, tag2);
  }

  @Test
  public void should_create_empty_tag() {
    Tag tag = new Tag();
    assertNull(tag.getName());
    assertNull(tag.getId());
  }

  @Test
  public void should_set_name() {
    Tag tag = new Tag();
    tag.setName("kotlin");
    assertEquals("kotlin", tag.getName());
  }

  @Test
  public void should_set_id() {
    Tag tag = new Tag();
    tag.setId("custom-id");
    assertEquals("custom-id", tag.getId());
  }

  @Test
  public void should_have_same_hashcode_for_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    Tag tag = new Tag("java");
    String str = tag.toString();
    assertNotNull(str);
    assertTrue(str.contains("java"));
  }

  @Test
  public void should_not_equal_null() {
    Tag tag = new Tag("java");
    assertNotEquals(null, tag);
  }

  @Test
  public void should_equal_self() {
    Tag tag = new Tag("java");
    assertEquals(tag, tag);
  }
}
