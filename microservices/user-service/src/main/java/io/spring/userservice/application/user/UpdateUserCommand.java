package io.spring.userservice.application.user;

import io.spring.userservice.core.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@UpdateUserConstraint
public class UpdateUserCommand {

  private User targetUser;
  private UpdateUserParam param;
}
