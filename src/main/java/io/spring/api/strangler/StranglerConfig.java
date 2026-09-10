package io.spring.api.strangler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class StranglerConfig {

  @Bean
  public FilterRegistrationBean<StranglerProxyFilter> stranglerProxyFilter(
      StranglerProperties properties,
      @Value("${strangler.connect-timeout-ms:2000}") int connectTimeoutMillis,
      @Value("${strangler.read-timeout-ms:5000}") int readTimeoutMillis) {
    FilterRegistrationBean<StranglerProxyFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(
        new StranglerProxyFilter(properties, connectTimeoutMillis, readTimeoutMillis));
    registration.addUrlPatterns("/articles/*");
    // Runs ahead of Spring Security: authentication for routed paths is the extracted
    // service's responsibility, exactly as it is for the in-process controllers.
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
