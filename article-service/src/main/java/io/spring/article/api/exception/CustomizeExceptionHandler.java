package io.spring.article.api.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
                  Map<String, Object> error = new HashMap<>();
                  error.put("resource", fieldError.getObjectName());
                  error.put("field", fieldError.getField());
                  error.put("code", fieldError.getDefaultMessage());
                  return error;
                })
            .collect(Collectors.toList());

    Map<String, Object> body = new HashMap<>();
    body.put("errors", fieldErrors.isEmpty() ? Map.of("body", List.of("invalid")) : fieldErrors);

    return handleExceptionInternal(e, body, new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY, request);
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
