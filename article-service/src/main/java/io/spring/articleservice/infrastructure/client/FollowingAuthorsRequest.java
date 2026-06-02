package io.spring.articleservice.infrastructure.client;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowingAuthorsRequest {
  private String userId;
  private List<String> authorIds;
}
