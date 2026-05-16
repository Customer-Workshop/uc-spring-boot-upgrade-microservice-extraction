package io.spring.userservice.core.service;

import io.spring.userservice.core.user.User;

public class AuthorizationService {
  public static boolean canWriteUser(User currentUser, User targetUser) {
    return currentUser.getId().equals(targetUser.getId());
  }
}
