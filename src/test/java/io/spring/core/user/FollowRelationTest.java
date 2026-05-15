package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user1", "user2");

    assertEquals("user1", relation.getUserId());
    assertEquals("user2", relation.getTargetId());
  }

  @Test
  void should_be_equal_when_same_user_and_target() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user2");

    assertEquals(r1, r2);
    assertEquals(r1.hashCode(), r2.hashCode());
  }

  @Test
  void should_not_be_equal_when_different_user() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user3", "user2");

    assertNotEquals(r1, r2);
  }

  @Test
  void should_not_be_equal_when_different_target() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user3");

    assertNotEquals(r1, r2);
  }

  @Test
  void should_set_and_get_fields() {
    FollowRelation relation = new FollowRelation("user1", "user2");
    relation.setUserId("user3");
    relation.setTargetId("user4");

    assertEquals("user3", relation.getUserId());
    assertEquals("user4", relation.getTargetId());
  }
}
