package com.quoc.bank.common.exception;

import com.quoc.bank.common.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        log.warn("Business Exception: {} - {}", ex.getErrorCode().name(), ex.getMessage());
        ErrorCode errorCode = ex.getErrorCode();

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(ex.getMessage() != null ? ex.getMessage() : errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String enumKey = fieldError != null ? fieldError.getDefaultMessage() : "INVALID_KEY";

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        Map<String, Object> attributes = null;
        String resolvedMessage = null;

        try {
            if (enumKey != null) {
                errorCode = ErrorCode.valueOf(enumKey);
            }
            if (!ex.getBindingResult().getAllErrors().isEmpty()) {
                var constraintViolation = ex.getBindingResult()
                        .getAllErrors().getFirst().unwrap(ConstraintViolation.class);
                attributes = constraintViolation.getConstraintDescriptor().getAttributes();
            }
        } catch (Exception ignored) {
            if (fieldError != null) {
                resolvedMessage = fieldError.getField() + ": " + fieldError.getDefaultMessage();
            }
        }

        if (resolvedMessage == null) {
            resolvedMessage = Objects.nonNull(attributes)
                    ? mapAttribute(errorCode.getMessage(), attributes)
                    : errorCode.getMessage();
        }

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(resolvedMessage)
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(ErrorCode.VALIDATION_ERROR.getStatusCode())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Uncategorized Exception: ", ex);
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage() + ": " + ex.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getStatusCode())
                .body(response);
    }

    private String mapAttribute(String message, Map<String, Object> attributes) {
        if (attributes.containsKey(MIN_ATTRIBUTE)) {
            String minValue = String.valueOf(attributes.get(MIN_ATTRIBUTE));
            return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
        }
        return message;
    }
}
