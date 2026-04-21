package io.spring.commentservice.api.security;

import io.spring.commentservice.client.UserServiceClient;
import io.spring.commentservice.client.UserServiceClient.UserResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTokenFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserServiceClient userServiceClient;

  public JwtTokenFilter(JwtService jwtService, UserServiceClient userServiceClient) {
    this.jwtService = jwtService;
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    getTokenFromHeader(request)
        .ifPresent(
            token -> {
              jwtService
                  .getSubFromToken(token)
                  .ifPresent(
                      userId -> {
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                          Optional<UserResponse> userOpt = userServiceClient.getUserById(userId);
                          userOpt.ifPresent(
                              user -> {
                                AuthUserDetails authUser =
                                    new AuthUserDetails(user.getId(), user.getUsername());
                                UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                        authUser,
                                        null,
                                        Collections.singletonList(
                                            new SimpleGrantedAuthority("ROLE_USER")));
                                authToken.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authToken);
                              });
                        }
                      });
            });
    filterChain.doFilter(request, response);
  }

  private Optional<String> getTokenFromHeader(HttpServletRequest request) {
    String authorization = request.getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("Token ")) {
      return Optional.empty();
    }
    return Optional.of(authorization.substring("Token ".length()));
  }
}
