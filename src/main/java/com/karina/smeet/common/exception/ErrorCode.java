package com.karina.smeet.common.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(1002, "You do not have Permission", HttpStatus.FORBIDDEN),
    INVALID_REFRESH_TOKEN(1003, "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS(1004, "Email already exists", HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS(1005, "Username already exists", HttpStatus.CONFLICT),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(1007, "Refresh Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(1008, "Invalid email or password", HttpStatus.UNAUTHORIZED),
    ;
    private int code;
    private String message;
    HttpStatusCode httpStatusCode;

     ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}
