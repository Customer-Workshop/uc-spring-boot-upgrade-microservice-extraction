package io.spring.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

  private final SecretKey signingKey;

  public JwtAuthenticationFilter(@Value("${jwt.secret}") String secret) {
    SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
    this.signingKey = new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName());
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Token ")) {
      String token = authHeader.substring(6);
      Optional<String> userId = getUserIdFromToken(token);
      if (userId.isPresent()) {
        ServerHttpRequest modifiedRequest =
            exchange
                .getRequest()
                .mutate()
                .header("X-User-Id", userId.get())
                .build();
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
      }
    }
    return chain.filter(exchange);
  }

  private Optional<String> getUserIdFromToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
