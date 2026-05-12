package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_with_constructor() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  public void should_create_with_no_args_constructor() {
    FollowRelation relation = new FollowRelation();

    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  public void should_set_values() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user-id");
    relation.setTargetId("target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    FollowRelation r1 = new FollowRelation("user1", "target1");
    FollowRelation r2 = new FollowRelation("user1", "target1");

    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  public void should_not_equal_different_relation() {
    FollowRelation r1 = new FollowRelation("user1", "target1");
    FollowRelation r2 = new FollowRelation("user1", "target2");

    assertNotEquals(r1, r2);
  }

  @Test
  public void should_implement_toString() {
    FollowRelation relation = new FollowRelation("user1", "target1");

    String str = relation.toString();

    assertNotNull(str);
    assertTrue(str.contains("user1"));
  }
}
