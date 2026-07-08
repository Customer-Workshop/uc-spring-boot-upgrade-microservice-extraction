package io.spring;

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
   * RestTemplate used for service-to-service calls to the Comments microservice. It uses a
   * dedicated {@link ObjectMapper} so it is not affected by the application's global {@code
   * UNWRAP_ROOT_VALUE} Jackson setting (which is meant for the monolith's own request envelopes).
   */
  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    RestTemplate restTemplate =
        builder
            .setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(5))
            .build();
    restTemplate
        .getMessageConverters()
        .removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
    restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter(new ObjectMapper()));
    return restTemplate;
  }
}
