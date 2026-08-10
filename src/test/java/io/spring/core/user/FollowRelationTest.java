package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class FollowRelationTest {

  @Test
  public void should_set_all_fields_in_constructor() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");
    assertThat(relation.getUserId(), is("user-id"));
    assertThat(relation.getTargetId(), is("target-id"));
  }

  @Test
  public void should_be_equal_when_both_ids_are_same() {
    FollowRelation relation = new FollowRelation("user-id", "target-id");
    FollowRelation same = new FollowRelation("user-id", "target-id");
    assertThat(relation.equals(same), is(true));
    assertThat(relation.hashCode(), is(same.hashCode()));
    assertThat(relation.equals(new FollowRelation("user-id", "other-target")), is(false));
  }
}
