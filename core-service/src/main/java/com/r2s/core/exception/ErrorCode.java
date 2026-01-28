package com.r2s.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    TOO_MANY_REQUEST(429, "Too many requests", HttpStatus.TOO_MANY_REQUESTS),

    UNAUTHORIZED(1001, "You do not have permission", HttpStatus.UNAUTHORIZED),
    TOKEN_GENERATION_FAILED(1002, "Token generation failed", HttpStatus.UNAUTHORIZED),
    ACCOUNT_NOT_FOUND(1003, "Account not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(1004, "Invalid request", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1005, "Role not found", HttpStatus.NOT_FOUND),

    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_EXISTS(2002, "User exists", HttpStatus.BAD_REQUEST),

    PASSWORD_INVALID(2003, "Password is invalid", HttpStatus.UNAUTHORIZED),
    ;

    private final int code;

    private final String message;

    private final HttpStatusCode status;

    ErrorCode(int code, String message, HttpStatusCode status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
