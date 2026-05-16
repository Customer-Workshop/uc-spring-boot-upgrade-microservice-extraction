package io.spring.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
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

  private final String secret;
  private final UserLookup userLookup;

  public JwtTokenFilter(String secret, UserLookup userLookup) {
    this.secret = secret;
    this.userLookup = userLookup;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    getTokenFromHeader(request)
        .flatMap(
            token -> {
              try {
                Jws<Claims> claimsJws =
                    Jwts.parserBuilder()
                        .setSigningKey(
                            io.jsonwebtoken.security.Keys.hmacShaKeyFor(secret.getBytes()))
                        .build()
                        .parseClaimsJws(token);
                String userId = claimsJws.getBody().getSubject();
                return userLookup.findById(userId);
              } catch (Exception e) {
                return Optional.empty();
              }
            })
        .ifPresent(
            user -> {
              UsernamePasswordAuthenticationToken authToken =
                  new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
              authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
              SecurityContextHolder.getContext().setAuthentication(authToken);
            });
    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenFromHeader(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization != null && authorization.startsWith("Token ")) {
      return Optional.of(authorization.substring(6));
    }
    return Optional.empty();
  }
}
