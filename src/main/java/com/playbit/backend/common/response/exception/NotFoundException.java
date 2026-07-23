package com.playbit.backend.common.response.exception;

import com.playbit.backend.common.response.ErrorCode;
import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    ErrorCode errorCode;
    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
