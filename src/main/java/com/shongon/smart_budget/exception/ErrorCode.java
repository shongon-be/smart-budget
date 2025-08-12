package com.shongon.smart_budget.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {

    UNCATEGORIZED(500, HttpStatus.INTERNAL_SERVER_ERROR, "An unknown error occurred"),

    INVALID_TOKEN(401, HttpStatus.UNAUTHORIZED, "Invalid token"),
    TOKEN_EXPIRED(401, HttpStatus.UNAUTHORIZED, "Token expired"),

    EMAIL_ALREADY_EXISTS(400, HttpStatus.BAD_REQUEST, "Email already exists"),
    USERNAME_ALREADY_EXISTS(400, HttpStatus.BAD_REQUEST, "Username already exists"),
    INVALID_PASSWORD(400, HttpStatus.BAD_REQUEST, "Invalid password"),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "User not found"),


    ;

    private final int code;
    private final HttpStatusCode statusCode;
    private final String message;

    ErrorCode(int code, HttpStatusCode statusCode, String message) {
        this.code = code;
        this.statusCode = statusCode;
        this.message = message;
    }
}
