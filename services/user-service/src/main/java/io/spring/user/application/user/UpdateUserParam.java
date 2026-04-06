package io.spring.user.application.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("user")
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserParam {
  private String email = "";
  private String username = "";
  private String password = "";
  private String bio = "";
  private String image = "";
}
