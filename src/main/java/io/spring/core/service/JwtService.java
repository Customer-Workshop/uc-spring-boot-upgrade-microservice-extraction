package io.spring.core.service;

import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Converts users to JWTs and extracts subjects from JWTs. */
@Service
public interface JwtService {
  /** Creates a token whose subject identifies the user. */
  String toToken(User user);

  /** Extracts the subject from a token, or returns empty when it cannot be parsed. */
  Optional<String> getSubFromToken(String token);
}
