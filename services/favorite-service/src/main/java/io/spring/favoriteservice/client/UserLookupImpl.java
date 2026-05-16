package io.spring.favoriteservice.client;

import io.spring.common.security.UserLookup;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserLookupImpl implements UserLookup {

  private final UserServiceClient userServiceClient;

  public UserLookupImpl(UserServiceClient userServiceClient) {
    this.userServiceClient = userServiceClient;
  }

  @Override
  public Optional<Object> findById(String userId) {
    return userServiceClient.getUserById(userId).map(userData -> userData);
  }
}
