package com.r2s.core_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1001, "You do not have permission", HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST(1003, "Invalid request", HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND(2001, "User not found", HttpStatus.NOT_FOUND),
    USER_EXISTS(2002, "User exists", HttpStatus.BAD_REQUEST),

    PASSWORD_INVALID(2003, "Password is invalid", HttpStatus.BAD_REQUEST),
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
