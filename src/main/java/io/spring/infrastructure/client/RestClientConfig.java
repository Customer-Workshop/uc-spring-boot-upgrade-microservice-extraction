package io.spring.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

  /**
   * RestTemplate used to talk to the comments microservice. It uses a dedicated {@link ObjectMapper}
   * so the monolith's global {@code UNWRAP_ROOT_VALUE} deserialization setting does not apply to the
   * plain JSON returned by the microservice.
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    restTemplate
        .getMessageConverters()
        .removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
    restTemplate
        .getMessageConverters()
        .add(new MappingJackson2HttpMessageConverter(new ObjectMapper()));
    return restTemplate;
  }
}
