package io.spring.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * Boots the whole application on a random port so that the scenarios exercise the real REST API,
 * security filters and MyBatis persistence.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cucumber")
public class CucumberSpringConfiguration {

  @TestConfiguration
  static class CucumberTestBeans {

    @Bean
    public ApiClient apiClient(Environment environment) {
      return new ApiClient(environment);
    }

    @Bean
    public ScenarioContext scenarioContext() {
      return new ScenarioContext();
    }

    @Bean
    public DatabaseCleaner databaseCleaner(JdbcTemplate jdbcTemplate) {
      return new DatabaseCleaner(jdbcTemplate);
    }
  }
}
