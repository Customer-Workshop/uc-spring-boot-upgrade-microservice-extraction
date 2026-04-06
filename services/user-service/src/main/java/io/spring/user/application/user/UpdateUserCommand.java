package io.spring.user.application.user;

import io.spring.user.core.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateUserCommand {
  private User targetUser;
  private UpdateUserParam param;
}
