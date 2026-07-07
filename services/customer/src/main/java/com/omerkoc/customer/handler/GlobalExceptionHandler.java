package com.omerkoc.customer.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.omerkoc.customer.exception.CustomerAlreadyExistsException;
import com.omerkoc.customer.exception.CustomerNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFoundException(
            CustomerNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "CUSTOMER_NOT_FOUND");
        errorMessage.put("status", HttpStatus.NOT_FOUND.value());
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCustomerAlreadyExistsException(
            CustomerAlreadyExistsException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "CUSTOMER_ALREADY_EXISTS");
        errorMessage.put("status", HttpStatus.BAD_REQUEST.value());
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }
}