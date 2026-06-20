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
    USER_NOT_EXISTED(1009, "USER NOT EXISTED", HttpStatus.NOT_FOUND),
    OTP_TOO_MANY_REQUESTS(1010, "Please wait 60 seconds before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    EXPIRED_OTP(1017, "OTP has expired, please request a new one", HttpStatus.BAD_REQUEST),
    INVALID_OTP(1018, "Invalid OTP code", HttpStatus.BAD_REQUEST),
    AUTH_PROVIDER_NOT_FOUND(1014, "Password login method for this account not found", HttpStatus.NOT_FOUND),
    CURRENT_PASSWORD_BLANK(1015, "Current Password cannot be empty", HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_BLANK(1016, "New Password cannot be empty", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(1017, "New Password must be at least 6 characters", HttpStatus.BAD_REQUEST),
    DISPLAY_NAME_BLANK(1018, "Display name cannot be empty", HttpStatus.BAD_REQUEST),
    DISPLAY_NAME_TOO_LONG(1019, "Display name must be at least 20 characters", HttpStatus.BAD_REQUEST),
    FILE_EMPTY(1020, "File is empty", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE(1021, "File size exceeds limit", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1022, "Invalid file type", HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED(1023, "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
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
