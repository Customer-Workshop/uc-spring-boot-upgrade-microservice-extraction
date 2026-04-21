package io.spring.article.core.service;

import java.util.Optional;

public interface JwtService {
  Optional<String> getSubFromToken(String token);
}
