package io.spring.api.exception;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Object> handleInvalidRequest(
      InvalidRequestException e, WebRequest request) {
    List<Map<String, Object>> fieldErrors =
        e.getErrors().getFieldErrors().stream()
            .map(
                fieldError -> {
                  Map<String, Object> map = new HashMap<>();
                  map.put("field", fieldError.getField());
                  map.put("message", fieldError.getDefaultMessage());
                  return map;
                })
            .collect(toList());

    Map<String, Object> body = new HashMap<>();
    body.put("errors", fieldErrors);
    return handleExceptionInternal(
        e, body, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException e, WebRequest request) {
    List<Map<String, Object>> errors = new ArrayList<>();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      Map<String, Object> map = new HashMap<>();
      map.put("field", violation.getPropertyPath().toString());
      map.put("message", violation.getMessage());
      errors.add(map);
    }
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errors);
    return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
