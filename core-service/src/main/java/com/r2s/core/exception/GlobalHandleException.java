package com.r2s.core.exception;

import com.r2s.core.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
        MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
            );

        ApiResponse<Map<String, String>> apiResponse = ApiResponse.<Map<String, String>>builder()
            .code(ErrorCode.INVALID_REQUEST.getCode())
            .message(ErrorCode.INVALID_REQUEST.getMessage())
            .result(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> handleAppException(AppException e) {
        ApiResponse<?> apiResponse = ApiResponse.builder()
            .code(e.getErrorCode().getCode())
            .message(e.getErrorCode().getMessage())
            .build();
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(apiResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<?>> handleRuntimeException(RuntimeException e) {

        if (e instanceof AccessDeniedException || e instanceof AuthenticationException) {
            throw e;
        }

        ApiResponse<?> apiResponse = ApiResponse.builder()
            .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
            .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(apiResponse);
    }
}