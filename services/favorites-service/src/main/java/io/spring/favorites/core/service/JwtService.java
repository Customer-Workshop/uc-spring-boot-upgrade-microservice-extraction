package io.spring.favorites.core.service;

import java.util.Optional;

public interface JwtService {
  Optional<String> getSubFromToken(String token);
}
