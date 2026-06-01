package io.spring.api.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

  private static final String HEADER_NAME = "X-Internal-Api-Key";

  @Value("${internal.api.key:default-internal-key}")
  private String expectedApiKey;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getRequestURI().startsWith("/api/internal/")) {
      String providedKey = request.getHeader(HEADER_NAME);
      if (providedKey == null || !providedKey.equals(expectedApiKey)) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Forbidden");
        return;
      }
    }
    filterChain.doFilter(request, response);
  }
}
