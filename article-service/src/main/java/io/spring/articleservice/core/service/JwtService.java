package io.spring.articleservice.core.service;

import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
  Optional<String> getSubFromToken(String token);
}
