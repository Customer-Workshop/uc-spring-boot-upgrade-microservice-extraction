package io.spring.comments.core.service;

import io.spring.comments.core.user.CurrentUser;
import java.util.Optional;

public interface JwtService {
  String toToken(CurrentUser user);

  Optional<String> getSubFromToken(String token);
}
