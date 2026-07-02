package io.spring.core.service;

import io.spring.core.user.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Service interface for JSON Web Token generation and parsing. */
@Service
public interface JwtService {

  /**
   * Generates a signed JWT for the given user.
   *
   * @param user the user to encode as the token subject
   * @return the compact JWT string
   */
  String toToken(User user);

  /**
   * Extracts the subject (user ID) from a JWT.
   *
   * @param token the compact JWT string
   * @return the user ID, or empty if the token is invalid or expired
   */
  Optional<String> getSubFromToken(String token);
}
