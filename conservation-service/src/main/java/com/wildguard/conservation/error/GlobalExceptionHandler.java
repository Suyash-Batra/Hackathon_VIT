package com.wildguard.conservation.error;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// All errors of my REST API come out in the same JSON shape:
// { "timestamp": ..., "status": 409, "message": "...", "fieldErrors": {...} }
// The pages read "message" and show it to the user.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // my own errors (400, 403, 404, 409)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return build(ex.getStatus(), ex.getMessage(), new LinkedHashMap<>());
    }

    // @Valid failed, for example an empty summary
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        String message = "Please correct the input:";
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (!fields.containsKey(error.getField())) {
                fields.put(error.getField(), error.getDefaultMessage());
                message = message + " " + error.getField() + " " + error.getDefaultMessage() + ";";
            }
        }
        return build(HttpStatus.BAD_REQUEST, message, fields);
    }

    // the JSON body could not be read (wrong date, wrong enum value ...)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "The request could not be read. Check dates, numbers and allowed values.", new LinkedHashMap<>());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parameter '" + ex.getName() + "' has an invalid value.",
                new LinkedHashMap<>());
    }

    // @PreAuthorize said no
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "Your role is not allowed to perform this action.", new LinkedHashMap<>());
    }

    // the database refused the row (for example the unique index on active allocations)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "The record conflicts with existing data.", new LinkedHashMap<>());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        // Spring's own errors (wrong url, wrong method) keep their status code
        if (ex instanceof ErrorResponse) {
            int code = ((ErrorResponse) ex).getStatusCode().value();
            return build(HttpStatus.valueOf(code), ex.getMessage(), new LinkedHashMap<>());
        }
        ex.printStackTrace();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error. Check the service log.",
                new LinkedHashMap<>());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        body.put("fieldErrors", fields);
        return ResponseEntity.status(status).body(body);
    }
}
