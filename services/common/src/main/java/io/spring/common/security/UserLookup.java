package io.spring.common.security;

import java.util.Optional;

public interface UserLookup {
  Optional<Object> findById(String userId);
}
