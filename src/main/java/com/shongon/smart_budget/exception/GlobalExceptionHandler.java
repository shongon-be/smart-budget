package com.shongon.smart_budget.exception;

import com.shongon.smart_budget.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handling unexpected error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Caught Exception: ", e);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ErrorCode.UNCATEGORIZED.getCode());
        errorResponse.setMessage(ErrorCode.UNCATEGORIZED.getMessage());

        return ResponseEntity.status(ErrorCode.UNCATEGORIZED.getStatusCode()).body(errorResponse);
    }

    // Handling application exceptions
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ErrorResponse> handlingAppException(AppException e) {
        log.error("Caught AppException: ", e);

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(errorCode.getCode());
        errorResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(errorResponse);
    }
}
