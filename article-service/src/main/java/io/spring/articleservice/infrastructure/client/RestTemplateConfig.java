package io.spring.articleservice.infrastructure.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

  @Value("${internal.api.key:default-internal-key}")
  private String internalApiKey;

  @Bean
  public RestTemplate restTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    ClientHttpRequestInterceptor apiKeyInterceptor =
        (request, body, execution) -> {
          request.getHeaders().set("X-Internal-Api-Key", internalApiKey);
          return execution.execute(request, body);
        };
    restTemplate.setInterceptors(List.of(apiKeyInterceptor));
    return restTemplate;
  }
}
