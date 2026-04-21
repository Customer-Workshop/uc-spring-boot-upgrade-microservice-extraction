package io.spring.commentservice.api.security;

import io.spring.commentservice.client.UserServiceClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  private final JwtService jwtService;
  private final UserServiceClient userServiceClient;

  public WebSecurityConfig(JwtService jwtService, UserServiceClient userServiceClient) {
    this.jwtService = jwtService;
    this.userServiceClient = userServiceClient;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
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
        .antMatchers(HttpMethod.GET, "/articles/*/comments")
        .permitAll()
        .anyRequest()
        .authenticated();

    http.addFilterBefore(
        new JwtTokenFilter(jwtService, userServiceClient),
        UsernamePasswordAuthenticationFilter.class);
  }

  @Bean
  public org.springframework.web.client.RestTemplate restTemplate() {
    return new org.springframework.web.client.RestTemplate();
  }
}
