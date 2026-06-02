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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomizeExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler({InvalidRequestException.class})
  public ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
    InvalidRequestException ire = (InvalidRequestException) e;

    List<FieldErrorResource> errorResources =
        ire.getErrors().getFieldErrors().stream()
            .map(
                fieldError -> {
                  FieldErrorResource fieldErrorResource = new FieldErrorResource();
                  fieldErrorResource.setResource(fieldError.getObjectName());
                  fieldErrorResource.setField(fieldError.getField());
                  fieldErrorResource.setCode(fieldError.getCode());
                  fieldErrorResource.setMessage(fieldError.getDefaultMessage());
                  return fieldErrorResource;
                })
            .collect(toList());

    ErrorResource error = new ErrorResource(errorResources);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(e, error, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolation(
      ConstraintViolationException e, WebRequest request) {
    Map<String, List<String>> errors = new HashMap<>();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      String field = violation.getPropertyPath().toString();
      String fieldName = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
      errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(violation.getMessage());
    }
    Map<String, Object> body = new HashMap<>();
    body.put("errors", errors);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    return handleExceptionInternal(e, body, headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
  }
}
