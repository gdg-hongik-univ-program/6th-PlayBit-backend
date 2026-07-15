package com.playbit.backend.common.response;

import lombok.Getter;
import org.springframework.web.ErrorResponse;

@Getter
public class ApiResponse<T>{
    private final boolean success;
    private final T data;
    private final ErrorResponse error;

    //constructor
    private ApiResponse(boolean success, T data, ErrorResponse error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    //method

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> error(ErrorResponse error) {
        return new ApiResponse<>(false, null, error);
    }

}
