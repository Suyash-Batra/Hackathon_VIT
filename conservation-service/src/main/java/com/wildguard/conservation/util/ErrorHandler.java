package com.wildguard.conservation.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// catches the exceptions thrown in my controllers and services and sends them back as
// { "status": 409, "message": "..." }  so the page can show the message
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> appError(AppException ex) {
        return answer(ex.getStatus(), ex.getMessage());
    }

    // the JSON could not be read: wrong date, text in place of a number etc.
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, Object>> badInput(Exception ex) {
        return answer(400, "The request could not be read. Check dates, numbers and allowed values.");
    }

    // the database said no (for example the unique index on active allocations)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> databaseError(DataIntegrityViolationException ex) {
        return answer(409, "The record conflicts with existing data.");
    }

    private ResponseEntity<Map<String, Object>> answer(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    // my own exception. status is the HTTP status code that should go back to the page
    // (400 wrong input, 401 not logged in, 403 role not allowed, 404 not found, 409 breaks a rule)
    public static class AppException extends RuntimeException {

        private int status;

        public AppException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
