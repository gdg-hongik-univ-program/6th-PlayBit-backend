package com.playbit.backend.common.exception;

import com.playbit.backend.common.ErrorCode;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    ErrorCode errorCode;
    public BadRequestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
