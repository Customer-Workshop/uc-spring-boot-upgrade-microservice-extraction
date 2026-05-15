package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user1", "user2");
    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  public void should_create_empty_follow_relation() {
    FollowRelation relation = new FollowRelation();
    assertNull(relation.getUserId());
    assertNull(relation.getTargetId());
  }

  @Test
  public void should_set_user_id() {
    FollowRelation relation = new FollowRelation();
    relation.setUserId("user1");
    assertEquals("user1", relation.getUserId());
  }

  @Test
  public void should_set_target_id() {
    FollowRelation relation = new FollowRelation();
    relation.setTargetId("target1");
    assertEquals("target1", relation.getTargetId());
  }

  @Test
  public void should_implement_equals() {
    FollowRelation r1 = new FollowRelation("user1", "target1");
    FollowRelation r2 = new FollowRelation("user1", "target1");
    assertEquals(r1, r2);
  }

  @Test
  public void should_not_equal_different_relation() {
    FollowRelation r1 = new FollowRelation("user1", "target1");
    FollowRelation r2 = new FollowRelation("user2", "target2");
    assertNotEquals(r1, r2);
  }

  @Test
  public void should_implement_hashcode() {
    FollowRelation r1 = new FollowRelation("user1", "target1");
    FollowRelation r2 = new FollowRelation("user1", "target1");
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  public void should_implement_tostring() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    String str = relation.toString();
    assertNotNull(str);
    assertTrue(str.contains("user1"));
  }

  @Test
  public void should_equal_self() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    assertEquals(relation, relation);
  }

  @Test
  public void should_not_equal_null() {
    FollowRelation relation = new FollowRelation("user1", "target1");
    assertNotEquals(null, relation);
  }
}
