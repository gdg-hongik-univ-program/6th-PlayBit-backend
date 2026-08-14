package com.playbit.backend.common.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    ErrorCode errorCode;

    public NotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
