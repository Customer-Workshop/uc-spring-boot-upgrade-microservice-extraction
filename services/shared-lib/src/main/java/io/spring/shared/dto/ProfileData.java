package io.spring.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileData {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;
  private String username;
  private String bio;
  private String image;
  private boolean following;
}
