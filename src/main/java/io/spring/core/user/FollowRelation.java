package io.spring.core.user;

// TODO: This entity has been extracted to the Profile Service (profile-service/).
// The Profile Service owns the follows table and the FollowRelation entity.
// This file can be removed once the Profile Service is fully deployed.
// See: profile-service/src/main/java/io/spring/profileservice/core/FollowRelation.java

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FollowRelation {
  private String userId;
  private String targetId;

  public FollowRelation(String userId, String targetId) {

    this.userId = userId;
    this.targetId = targetId;
  }
}
