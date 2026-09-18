package com.budzet.global.api;

import com.budzet.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(
        int resultCode,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), message, data);
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getStatus().value(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.getStatus().value(), errorCode.getMessage(), data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> response(HttpStatus status, String message, T data) {
        return ResponseEntity
                .status(status)
                .body(ApiResponse.success(status, message, data));
    }
}
