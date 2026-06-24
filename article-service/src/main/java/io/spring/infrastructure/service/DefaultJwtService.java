package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.spring.core.service.JwtService;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultJwtService implements JwtService {
  private final SecretKey signingKey;

  @Autowired
  public DefaultJwtService(@Value("${jwt.secret}") String secret) {
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      Claims claims =
          Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
      return Optional.ofNullable(claims.getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
