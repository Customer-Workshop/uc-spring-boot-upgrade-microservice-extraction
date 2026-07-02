package io.spring.core.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TagTest {

  @Test
  public void should_create_tag_with_name_and_id() {
    Tag tag = new Tag("java");
    assertEquals("java", tag.getName());
    assertNotNull(tag.getId());
  }

  @Test
  public void should_have_equality_based_on_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1, tag2);
  }

  @Test
  public void should_not_equal_tag_with_different_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1, tag2);
  }

  @Test
  public void should_have_consistent_hashcode_for_same_name() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    assertEquals(tag1.hashCode(), tag2.hashCode());
    assertNotEquals(0, tag1.hashCode());
  }

  @Test
  public void should_have_different_hashcodes_for_different_names() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("spring");
    assertNotEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_not_equal_null_or_different_type() {
    Tag tag = new Tag("java");
    assertNotEquals(tag, null);
    assertNotEquals(tag, "java");
  }

  @Test
  public void should_equal_itself() {
    Tag tag = new Tag("java");
    assertEquals(tag, tag);
  }

  @Test
  public void should_handle_equality_with_null_name() {
    Tag tag1 = new Tag();
    Tag tag2 = new Tag();
    assertEquals(tag1, tag2);
    assertEquals(tag1.hashCode(), tag2.hashCode());
  }

  @Test
  public void should_not_equal_null_name_to_non_null_name() {
    Tag withName = new Tag("java");
    Tag withoutName = new Tag();
    assertNotEquals(withName, withoutName);
    assertNotEquals(withoutName, withName);
  }

  @Test
  public void should_use_name_based_equality_in_collections() {
    Tag tag1 = new Tag("java");
    Tag tag2 = new Tag("java");
    java.util.Set<Tag> set = new java.util.HashSet<>();
    set.add(tag1);
    set.add(tag2);
    assertEquals(1, set.size());
    assertTrue(set.contains(tag1));
    assertTrue(set.contains(tag2));
  }

  @Test
  public void should_include_prime_factor_in_hashcode() {
    Tag tag = new Tag("java");
    int nameHash = tag.getName().hashCode();
    assertEquals(59 + nameHash, tag.hashCode());
  }

  @Test
  public void should_have_meaningful_toString() {
    Tag tag = new Tag("java");
    String str = tag.toString();
    assertNotNull(str);
    assertTrue(str.contains("java"));
  }
}
