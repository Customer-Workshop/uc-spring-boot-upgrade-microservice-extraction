package io.spring.articleservice.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.spring.articleservice.core.service.JwtService;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DefaultJwtService implements JwtService {

  @Value("${jwt.secret}")
  private String secret;

  private SecretKey signingKey;

  @PostConstruct
  public void init() {
    signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  @Override
  public Optional<String> getSubFromToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
