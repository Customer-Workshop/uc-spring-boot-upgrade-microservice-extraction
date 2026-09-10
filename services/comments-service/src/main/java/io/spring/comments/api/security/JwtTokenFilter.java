package io.spring.comments.api.security;

import io.spring.comments.api.exception.MonolithUnavailableException;
import io.spring.comments.api.exception.ResourceNotFoundException;
import io.spring.comments.core.service.JwtService;
import io.spring.comments.core.user.CurrentUser;
import io.spring.comments.infrastructure.monolith.MonolithClient;
import io.spring.comments.infrastructure.monolith.dto.ProfileDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTokenFilter extends OncePerRequestFilter {
  private final MonolithClient monolithClient;
  private final JwtService jwtService;
  private static final String HEADER = "Authorization";

  public JwtTokenFilter(MonolithClient monolithClient, JwtService jwtService) {
    this.monolithClient = monolithClient;
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> userId =
        getTokenString(request.getHeader(HEADER)).flatMap(jwtService::getSubFromToken);
    if (userId.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
      try {
        ProfileDTO profile = monolithClient.findProfileById(userId.get(), userId.get());
        CurrentUser currentUser =
            new CurrentUser(
                profile.getId(), profile.getUsername(), profile.getBio(), profile.getImage());
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
      } catch (ResourceNotFoundException e) {
        // unknown principal: the request continues unauthenticated
      } catch (MonolithUnavailableException e) {
        response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage());
        return;
      }
    }

    filterChain.doFilter(request, response);
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
