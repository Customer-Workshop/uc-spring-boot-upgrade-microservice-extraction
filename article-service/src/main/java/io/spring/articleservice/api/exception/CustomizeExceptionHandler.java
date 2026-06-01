package io.spring.articleservice.api.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Object> handleInvalidRequest(InvalidRequestException e) {
    List<Map<String, Object>> errorList =
        e.getErrors().getFieldErrors().stream()
            .map(
                fieldError -> {
                  Map<String, Object> map = new HashMap<>();
                  map.put("field", fieldError.getField());
                  map.put("message", fieldError.getDefaultMessage());
                  return map;
                })
            .collect(Collectors.toList());
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorList);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    List<Map<String, Object>> errorList =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError -> {
                  Map<String, Object> map = new HashMap<>();
                  map.put("field", fieldError.getField());
                  map.put("message", fieldError.getDefaultMessage());
                  return map;
                })
            .collect(Collectors.toList());
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorList);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
  }
}
