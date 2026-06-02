package io.spring.api.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FieldErrorResource {
  private String resource;
  private String field;
  private String code;
  private String message;
}
