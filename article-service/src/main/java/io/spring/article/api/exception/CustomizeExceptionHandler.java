package io.spring.article.api.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<Object> handleInvalidRequest(
      InvalidRequestException e, WebRequest request) {
    Map<String, List<String>> grouped = new HashMap<>();
    for (FieldError fe : e.getErrors().getFieldErrors()) {
      grouped.computeIfAbsent(fe.getField(), k -> new ArrayList<>()).add(fe.getDefaultMessage());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("errors", grouped.isEmpty() ? Map.of("body", List.of("invalid")) : grouped);

    return handleExceptionInternal(
        e, body, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    Map<String, List<String>> grouped = new HashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      grouped.computeIfAbsent(fe.getField(), k -> new ArrayList<>()).add(fe.getDefaultMessage());
    }

    Map<String, Object> body = new HashMap<>();
    body.put("errors", grouped.isEmpty() ? Map.of("body", List.of("invalid")) : grouped);

    return handleExceptionInternal(ex, body, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler(NoAuthorizationException.class)
  public ResponseEntity<Object> handleNoAuthorization(
      NoAuthorizationException e, WebRequest request) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Object> handleNotFound(
      ResourceNotFoundException e, WebRequest request) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }
}
