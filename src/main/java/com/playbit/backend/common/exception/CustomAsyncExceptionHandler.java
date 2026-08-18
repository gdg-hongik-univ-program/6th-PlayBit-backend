package com.playbit.backend.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Slf4j
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("비동기 에러 발생");
        log.error("실패한 메서드명: {}", method.getName());
        log.error("에러 메시지: {}", ex.getMessage());
    }
}
