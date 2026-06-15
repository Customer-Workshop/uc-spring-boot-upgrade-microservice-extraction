package io.spring.infrastructure.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Collections;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    ObjectMapper plainMapper = new ObjectMapper();
    plainMapper.disable(DeserializationFeature.UNWRAP_ROOT_VALUE);
    MappingJackson2HttpMessageConverter converter =
        new MappingJackson2HttpMessageConverter(plainMapper);

    RestTemplate restTemplate =
        builder
            .setConnectTimeout(Duration.ofSeconds(5))
            .setReadTimeout(Duration.ofSeconds(10))
            .build();
    restTemplate.setMessageConverters(Collections.singletonList(converter));
    return restTemplate;
  }
}
