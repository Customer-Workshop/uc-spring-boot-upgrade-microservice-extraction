package io.spring.core.service;

import io.spring.client.UserDto;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
  String toToken(UserDto user);

  Optional<String> getSubFromToken(String token);
}
