package io.spring.commentservice.api.exception;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
    List<Map<String, String>> errorList =
        e.getBindingResult().getFieldErrors().stream()
            .map(
                fieldError -> {
                  Map<String, String> error = new HashMap<>();
                  error.put("field", fieldError.getField());
                  error.put("message", fieldError.getDefaultMessage());
                  return error;
                })
            .collect(Collectors.toList());

    Map<String, Object> body = new HashMap<>();
    body.put("errors", errorList);
    return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(body);
  }
}
