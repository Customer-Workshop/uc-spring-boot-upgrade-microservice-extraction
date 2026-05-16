package io.spring.userservice.infrastructure;

import io.spring.common.security.UserLookup;
import io.spring.userservice.core.user.UserRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserLookupImpl implements UserLookup {

  private final UserRepository userRepository;

  public UserLookupImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Optional<Object> findById(String userId) {
    return userRepository.findById(userId).map(user -> user);
  }
}
