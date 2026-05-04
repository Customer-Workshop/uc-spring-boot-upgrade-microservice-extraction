package com.bank.rm.common.dto;

import java.time.Instant;

public record ApiResponse<T>(
    boolean success,
    T data,
    ErrorDetail error,
    Instant timestamp
) {
    public ApiResponse(boolean success, T data) {
        this(success, data, null, Instant.now());
    }

    public ApiResponse(boolean success, T data, ErrorDetail error) {
        this(success, data, error, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message, null, null));
    }
}
