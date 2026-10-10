package com.wildguard.conservation.error;

import org.springframework.http.HttpStatus;

// My own exception for "the request cannot be done".
// It carries the HTTP status code, and GlobalExceptionHandler turns it into a JSON answer.
public class ApiException extends RuntimeException {

    private HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    // 400 = the input is wrong
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    // 403 = this role is not allowed
    public static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, message);
    }

    // 404 = the record does not exist
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    // 409 = it breaks a rule (for example the vehicle is already allocated)
    public static ApiException conflict(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }
}
