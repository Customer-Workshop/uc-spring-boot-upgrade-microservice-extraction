package io.spring.infrastructure.service.comments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentServiceResponse {
  private String id;
  private String body;
  private String articleId;
  private String userId;
  private String createdAt;
  private String updatedAt;
}
