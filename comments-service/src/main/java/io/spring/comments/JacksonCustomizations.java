package io.spring.comments;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonCustomizations {

  @Bean
  public Module jodaModule() {
    return new JodaModule();
  }
}
