package io.spring.common.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({InvalidRequestException.class})
  public ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
    InvalidRequestException ire = (InvalidRequestException) e;
    Map<String, List<String>> errorsMap = new HashMap<>();
    ire.getErrors()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              errorsMap
                  .computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
                  .add(fieldError.getDefaultMessage());
            });
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorsMap);
    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(body);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException e, WebRequest request) {
    Map<String, List<String>> errorsMap = new HashMap<>();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      String field = violation.getPropertyPath().toString();
      errorsMap.computeIfAbsent(field, k -> new ArrayList<>()).add(violation.getMessage());
    }
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorsMap);
    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    Map<String, List<String>> errorsMap = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError -> {
              errorsMap
                  .computeIfAbsent(fieldError.getField(), k -> new ArrayList<>())
                  .add(fieldError.getDefaultMessage());
            });
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorsMap);
    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(body);
  }
}
