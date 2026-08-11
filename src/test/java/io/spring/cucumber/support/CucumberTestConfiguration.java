package io.spring.cucumber.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class CucumberTestConfiguration {

  @Bean
  @ScenarioScope
  public ApiContext apiContext() {
    return new ApiContext();
  }
}
