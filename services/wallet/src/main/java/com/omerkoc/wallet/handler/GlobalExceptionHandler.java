package com.omerkoc.wallet.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.omerkoc.wallet.exception.WalletAlreadyExistsException;
import com.omerkoc.wallet.exception.WalletNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleWalletNotFoundException(
            WalletNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "WALLET_NOT_FOUND");
        errorMessage.put("status", HttpStatus.NOT_FOUND.value());
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleWalletAlreadyExistsException(
            WalletAlreadyExistsException ex,
            HttpServletRequest request) {

        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("message", ex.getMessage());
        errorMessage.put("code", "WALLET_ALREADY_EXISTS");
        errorMessage.put("status", HttpStatus.BAD_REQUEST.value());
        errorMessage.put("timestamp", System.currentTimeMillis());
        errorMessage.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(errorMessage));
    }
}
