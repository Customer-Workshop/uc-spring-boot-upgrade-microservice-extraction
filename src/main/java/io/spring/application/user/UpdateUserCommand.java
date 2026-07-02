package io.spring.application.user;

import io.spring.core.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command object pairing the target user with the update parameters. Validated via {@link
 * UpdateUserConstraint} to ensure email and username uniqueness.
 */
@Getter
@AllArgsConstructor
@UpdateUserConstraint
public class UpdateUserCommand {

  private User targetUser;
  private UpdateUserParam param;
}
