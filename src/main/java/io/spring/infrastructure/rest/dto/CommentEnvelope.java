package io.spring.infrastructure.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response envelopes used by the comments microservice REST API. */
public class CommentEnvelope {

  @Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Single {
    private CommentResponse comment;
  }

  @Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Multiple {
    private List<CommentResponse> comments;
  }
}
