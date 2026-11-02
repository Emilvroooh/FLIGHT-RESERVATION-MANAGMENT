package com.skyward.api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler({IllegalArgumentException.class,IllegalStateException.class}) ResponseEntity<?> badRequest(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()==null?"Request could not be completed":e.getMessage()));}
 @ExceptionHandler(DuplicateKeyException.class) ResponseEntity<?> conflict(DuplicateKeyException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","An account or flight with those unique details already exists."));}
 @ExceptionHandler({BadCredentialsException.class,SecurityException.class}) ResponseEntity<?> unauthorized(RuntimeException e){return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message",e instanceof SecurityException?e.getMessage():"Email or password was not recognized."));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){return ResponseEntity.badRequest().body(Map.of("message","Please check the submitted fields."));}
 @ExceptionHandler(Exception.class) ResponseEntity<?> server(Exception e){return ResponseEntity.status(500).body(Map.of("message","The request could not be completed."));}
}
