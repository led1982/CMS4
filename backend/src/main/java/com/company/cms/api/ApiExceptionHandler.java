package com.company.cms.api;

import com.company.cms.api.dto.CmsDtos.ErrorResponse;
import com.company.cms.api.dto.CmsDtos.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.status())
            .body(new ErrorResponse(ex.code(), ex.getMessage(), List.of(), request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
        var errors = ex instanceof MethodArgumentNotValidException methodArgument
            ? methodArgument.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList()
            : ((BindException) ex).getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_FAILED", "Request validation failed", errors, request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        var errors = ex.getConstraintViolations().stream()
            .map(violation -> new FieldError(violation.getPropertyPath().toString(), violation.getMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_FAILED", "Request validation failed", errors, request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_JSON", "Request body is not readable", List.of(), request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred", List.of(), request.getHeader("X-Request-Id")));
    }

    public static class ApiException extends RuntimeException {
        private final HttpStatus status;
        private final String code;

        public ApiException(HttpStatus status, String code, String message) {
            super(message);
            this.status = status;
            this.code = code;
        }

        public HttpStatus status() {
            return status;
        }

        public String code() {
            return code;
        }

        public static ApiException badRequest(String code, String message) {
            return new ApiException(HttpStatus.BAD_REQUEST, code, message);
        }

        public static ApiException forbidden(String message) {
            return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message);
        }

        public static ApiException notFound(String message) {
            return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message);
        }

        public static ApiException conflict(String message) {
            return new ApiException(HttpStatus.CONFLICT, "CONFLICT", message);
        }

        public static ApiException payloadTooLarge(String message) {
            return new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE", message);
        }
    }
}
