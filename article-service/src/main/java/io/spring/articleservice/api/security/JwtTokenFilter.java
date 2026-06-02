package io.spring.articleservice.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTokenFilter extends OncePerRequestFilter {
  private final SecretKey signingKey;
  private final String header = "Authorization";

  public JwtTokenFilter(@Value("${jwt.secret}") String secret) {
    this.signingKey =
        new SecretKeySpec(secret.getBytes(), io.jsonwebtoken.SignatureAlgorithm.HS512.getJcaName());
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    getTokenString(request.getHeader(header))
        .flatMap(this::getSubFromToken)
        .ifPresent(
            userId -> {
              if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
              }
            });

    filterChain.doFilter(request, response);
  }

  private Optional<String> getSubFromToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Optional<String> getTokenString(String header) {
    if (header == null) {
      return Optional.empty();
    } else {
      String[] split = header.split(" ");
      if (split.length < 2) {
        return Optional.empty();
      } else {
        return Optional.ofNullable(split[1]);
      }
    }
  }
}
