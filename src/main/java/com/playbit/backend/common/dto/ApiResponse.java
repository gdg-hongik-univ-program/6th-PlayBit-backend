package com.playbit.backend.common.dto;

public record ApiResponse<T>(boolean success, T data, ErrorResponse error) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<ErrorResponse> error(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }
}
