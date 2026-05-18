package io.spring.articleservice.api.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    List<HashMap<String, String>> errorList =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError -> {
                  HashMap<String, String> map = new HashMap<>();
                  map.put("field", fieldError.getField());
                  map.put("message", fieldError.getDefaultMessage());
                  return map;
                })
            .collect(Collectors.toList());
    HashMap<String, Object> body = new HashMap<>();
    body.put("errors", errorList);
    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(body);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  @ResponseStatus(UNPROCESSABLE_ENTITY)
  @ResponseBody
  public HashMap<String, Object> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest request) {
    List<HashMap<String, String>> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      HashMap<String, String> error = new HashMap<>();
      error.put("field", violation.getPropertyPath().toString());
      error.put("message", violation.getMessage());
      errors.add(error);
    }
    HashMap<String, Object> body = new HashMap<>();
    body.put("errors", errors);
    return body;
  }
}
