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
  public void should_equal_same_relation() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user2");
    assertEquals(r1, r2);
  }

  @Test
  public void should_not_equal_different_relation() {
    FollowRelation r1 = new FollowRelation("user1", "user2");
    FollowRelation r2 = new FollowRelation("user1", "user3");
    assertNotEquals(r1, r2);
  }
}
