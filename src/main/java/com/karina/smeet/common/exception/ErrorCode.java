package com.karina.smeet.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    // ── System ────────────────────────────────────────────────
    UNCATEGORIZED_EXCEPTION     (9999, "An unexpected error occurred",                      HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY                 (1001, "Invalid message key",                               HttpStatus.BAD_REQUEST),

    // ── Auth ──────────────────────────────────────────────────
    UNAUTHENTICATED             (1002, "Authentication required",                           HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED                (1003, "You do not have permission",                        HttpStatus.FORBIDDEN),
    INVALID_CREDENTIALS         (1004, "Invalid email or password",                         HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN       (1005, "Invalid refresh token",                             HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED       (1006, "Refresh token has expired",                         HttpStatus.UNAUTHORIZED),
    AUTH_PROVIDER_NOT_FOUND     (1007, "Password login not available for this account",     HttpStatus.NOT_FOUND),

    // ── User ──────────────────────────────────────────────────
    USER_NOT_EXISTED            (1010, "User not found",                                    HttpStatus.NOT_FOUND),
    EMAIL_ALREADY_EXISTS        (1011, "Email already exists",                              HttpStatus.CONFLICT),
    USERNAME_ALREADY_EXISTS     (1012, "Username already exists",                           HttpStatus.CONFLICT),

    // ── Password ──────────────────────────────────────────────
    CURRENT_PASSWORD_BLANK      (1020, "Current password cannot be empty",                  HttpStatus.BAD_REQUEST),
    NEW_PASSWORD_BLANK          (1021, "New password cannot be empty",                      HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT          (1022, "New password must be at least 6 characters",        HttpStatus.BAD_REQUEST),

    // ── Profile ───────────────────────────────────────────────
    DISPLAY_NAME_BLANK          (1030, "Display name cannot be empty",                      HttpStatus.BAD_REQUEST),
    DISPLAY_NAME_TOO_LONG       (1031, "Display name must not exceed 100 characters",       HttpStatus.BAD_REQUEST),

    // ── OTP ───────────────────────────────────────────────────
    OTP_TOO_MANY_REQUESTS       (1040, "Please wait 60 seconds before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    EXPIRED_OTP                 (1041, "OTP has expired, please request a new one",         HttpStatus.BAD_REQUEST),
    INVALID_OTP                 (1042, "Invalid OTP code",                                  HttpStatus.BAD_REQUEST),

    // ── File upload ───────────────────────────────────────────
    FILE_EMPTY                  (1050, "File cannot be empty",                              HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE              (1051, "File size exceeds the 5MB limit",                   HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE           (1052, "Only JPEG, PNG and WEBP files are allowed",         HttpStatus.BAD_REQUEST),
    UPLOAD_FAILED               (1053, "Failed to upload file, please try again",           HttpStatus.INTERNAL_SERVER_ERROR),

    // ── Friend ────────────────────────────────────────────────
    CANNOT_ADD_SELF             (1060, "You cannot send a friend request to yourself",      HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_ALREADY_SENT (1061, "A friend request has already been sent",            HttpStatus.CONFLICT),
    ALREADY_FRIENDS             (1062, "You are already friends with this user",            HttpStatus.CONFLICT),
    FRIENDSHIP_NOT_FOUND        (1063, "Friend relationship not found",                     HttpStatus.NOT_FOUND),
    INVALID_FRIENDSHIP_STATUS   (1064, "Friend request is no longer pending",               HttpStatus.BAD_REQUEST),
    FRIENDSHIP_CONFLICT         (1065, "A relationship with this user already exists",      HttpStatus.CONFLICT),

    // ── Room ──────────────────────────────────────────────────
    ROOM_NOT_FOUND               (1070, "Room not found",                                        HttpStatus.NOT_FOUND),
    NOT_ROOM_MEMBER              (1071, "You are not a member of this room",                     HttpStatus.FORBIDDEN),
    PERMISSION_DENIED            (1072, "You do not have permission to perform this action",     HttpStatus.FORBIDDEN),
    CANNOT_MODIFY_DIRECT_ROOM    (1080, "This action is not allowed on a direct message room",   HttpStatus.BAD_REQUEST),
    ALREADY_ROOM_MEMBER          (1081, "User is already a member of this room",                 HttpStatus.CONFLICT),
    CANNOT_KICK_SELF             (1082, "You cannot kick yourself, use leave instead",            HttpStatus.BAD_REQUEST),
    CANNOT_KICK_OWNER            (1083, "You cannot remove the room owner",                       HttpStatus.FORBIDDEN),
    CANNOT_CHANGE_OWN_ROLE       (1084, "You cannot change your own role",                        HttpStatus.BAD_REQUEST),
    NOT_FRIENDS                  (1085, "You can only message users who are your friends",        HttpStatus.FORBIDDEN),
    OWNER_CANNOT_LEAVE           (1086, "Transfer ownership to another member before leaving the room", HttpStatus.BAD_REQUEST),
    ROOM_MEMBER_CONFLICT         (1087, "This member was just modified by someone else, please retry", HttpStatus.CONFLICT),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode httpStatusCode;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }
}