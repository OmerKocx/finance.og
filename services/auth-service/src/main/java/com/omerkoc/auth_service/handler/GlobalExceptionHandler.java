package com.omerkoc.auth_service.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.hc.core5.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.omerkoc.auth_service.exception.EmailAlreadyExistsException;
import com.omerkoc.auth_service.exception.InvalidTokenException;
import com.omerkoc.auth_service.exception.PasswordMismatchException;
import com.omerkoc.auth_service.exception.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "EMAIL_ALREADY_EXISTS");
        errorMessage.put("status", HttpStatus.SC_BAD_REQUEST);
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
            InvalidTokenException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "INVALID_TOKEN");
        errorMessage.put("status", HttpStatus.SC_BAD_REQUEST);
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(
            UserNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "USER_NOT_FOUND");
        errorMessage.put("status", HttpStatus.SC_BAD_REQUEST);
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPasswordException(
            PasswordMismatchException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "INVALID_PASSWORD");
        errorMessage.put("status", HttpStatus.SC_BAD_REQUEST);
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }
}
