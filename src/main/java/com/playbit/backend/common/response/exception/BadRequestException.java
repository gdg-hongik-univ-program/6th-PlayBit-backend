package com.playbit.backend.common.response.exception;

import com.playbit.backend.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    ErrorCode errorCode;
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
