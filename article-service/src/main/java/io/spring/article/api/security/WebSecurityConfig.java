package io.spring.article.api.security;

import io.spring.article.core.service.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
  private JwtService jwtService;

  public WebSecurityConfig(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .cors()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        .and()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS)
        .permitAll()
        .antMatchers(HttpMethod.GET, "/articles/feed")
        .authenticated()
        .antMatchers(HttpMethod.POST, "/articles")
        .authenticated()
        .antMatchers(HttpMethod.POST, "/articles/*/comments")
        .authenticated()
        .antMatchers(HttpMethod.POST, "/articles/*/favorite")
        .authenticated()
        .antMatchers(HttpMethod.DELETE, "/articles/*/favorite")
        .authenticated()
        .antMatchers(HttpMethod.DELETE, "/articles/**")
        .authenticated()
        .antMatchers(HttpMethod.PUT, "/articles/**")
        .authenticated()
        .anyRequest()
        .permitAll();

    http.addFilterBefore(
        new JwtTokenFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
