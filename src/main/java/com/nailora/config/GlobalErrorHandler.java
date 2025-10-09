package com.nailora.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

@RestControllerAdvice
public class GlobalErrorHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String,Object>> handleRse(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(Map.of(
          "error", "ResponseStatusException",
          "status", e.getStatusCode().value(),
          "message", String.valueOf(e.getReason())
        ));
  }

  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      HttpMessageNotReadableException.class
  })
  public ResponseEntity<Map<String,Object>> handleBadRequest(Exception e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of(
          "error", e.getClass().getSimpleName(),
          "status", 400,
          "message", String.valueOf(e.getMessage())
        ));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String,Object>> handleDb(DataIntegrityViolationException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of(
          "error", "DataIntegrityViolation",
          "status", 409,
          "message", String.valueOf(e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage())
        ));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String,Object>> handleAny(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(
          "error", e.getClass().getSimpleName(),
          "status", 500,
          "message", String.valueOf(e.getMessage())
        ));
  }
}
