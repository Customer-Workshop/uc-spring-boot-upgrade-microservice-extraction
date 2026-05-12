package io.spring.favoriteservice.core.service;

import io.spring.favoriteservice.core.user.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
  String toToken(User user);

  Optional<String> getSubFromToken(String token);
}
