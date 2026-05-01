package io.spring.application.user;

import com.fasterxml.jackson.annotation.JsonRootName;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonRootName("user")
@AllArgsConstructor
@NoArgsConstructor
public class RegisterParam {
  @NotBlank(message = "can't be empty")
  @Email(message = "should be an email")
  @DuplicatedEmailConstraint
  private String email;

  @NotBlank(message = "can't be empty")
  @Size(min = 1, max = 100, message = "must be between 1 and 100 characters")
  @DuplicatedUsernameConstraint
  private String username;

  @NotBlank(message = "can't be empty")
  @Size(min = 8, max = 100, message = "must be between 8 and 100 characters")
  private String password;
}
