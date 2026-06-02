package io.spring.articleservice.core.service;

import java.util.Optional;

public interface JwtService {
  Optional<String> getSubFromToken(String token);
}
