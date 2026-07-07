package io.spring.infrastructure.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
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
   * Dedicated RestTemplate for talking to the comments microservice. It uses its own ObjectMapper
   * so it is unaffected by the monolith's global {@code UNWRAP_ROOT_VALUE} Jackson setting (which
   * is required for the public RealWorld API but would otherwise break parsing of the
   * microservice's {@code {"comment": ...}} / {@code {"comments": [...]}} envelopes).
   */
  @Bean
  public RestTemplate commentServiceRestTemplate(RestTemplateBuilder builder) {
    ObjectMapper objectMapper =
        new ObjectMapper()
            .disable(DeserializationFeature.UNWRAP_ROOT_VALUE)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    RestTemplate restTemplate =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    restTemplate
        .getMessageConverters()
        .removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(objectMapper));
    return restTemplate;
  }
}
