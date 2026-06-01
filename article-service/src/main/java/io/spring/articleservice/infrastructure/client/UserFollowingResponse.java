package io.spring.articleservice.infrastructure.client;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFollowingResponse {
  private boolean following;
  private Set<String> followingIds;
}
