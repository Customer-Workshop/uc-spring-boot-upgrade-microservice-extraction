package io.spring.core.user;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  void should_create_follow_relation() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");

    assertEquals("user-id", relation.getUserId());
    assertEquals("target-id", relation.getTargetId());
  }

  @Test
  void should_store_user_and_target_ids() {
    FollowRelation relation = new FollowRelation("follower", "following");

    assertEquals("follower", relation.getUserId());
    assertEquals("following", relation.getTargetId());
  }
}
