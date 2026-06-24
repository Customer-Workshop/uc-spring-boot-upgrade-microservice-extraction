package io.spring.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Raw comment projection returned by the comments microservice. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteComment {
  private String id;
  private String body;
  private String articleId;
  private String userId;
  private String createdAt;
}
