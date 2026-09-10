package io.spring.favorites.api.security;

import io.spring.favorites.api.exception.MonolithUnavailableException;
import io.spring.favorites.api.exception.ResourceNotFoundException;
import io.spring.favorites.core.service.JwtService;
import io.spring.favorites.core.user.CurrentUser;
import io.spring.favorites.infrastructure.monolith.MonolithClient;
import io.spring.favorites.infrastructure.monolith.dto.ProfileDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtTokenFilter extends OncePerRequestFilter {
  @Autowired private MonolithClient monolithClient;
  @Autowired private JwtService jwtService;
  private final String header = "Authorization";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    Optional<String> userId =
        getTokenString(request.getHeader(header))
            .flatMap(token -> jwtService.getSubFromToken(token));

    if (userId.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
      ProfileDto profile;
      try {
        profile = monolithClient.findProfileById(userId.get(), userId.get());
      } catch (ResourceNotFoundException e) {
        profile = null;
      } catch (MonolithUnavailableException e) {
        response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), e.getMessage());
        return;
      }
      if (profile != null) {
        CurrentUser currentUser =
            new CurrentUser(
                profile.getId() == null ? userId.get() : profile.getId(),
                profile.getUsername(),
                profile.getBio(),
                profile.getImage());
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(currentUser, null, Collections.emptyList());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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
