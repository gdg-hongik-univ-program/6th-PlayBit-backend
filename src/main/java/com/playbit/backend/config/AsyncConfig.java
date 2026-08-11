package com.playbit.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);   // 기본으로 유지할 일꾼 수
        executor.setMaxPoolSize(10);  // 최대 늘어날 수 있는 일꾼 수
        executor.setQueueCapacity(100); // 대기 줄 길이
        executor.setThreadNamePrefix("Async-Thread-");
        executor.initialize();
        return executor;
    }
}