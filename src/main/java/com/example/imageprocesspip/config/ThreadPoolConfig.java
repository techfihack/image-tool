package com.example.imageprocesspip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolConfig {

    @Bean
    public ExecutorService imageProcessingExecutor() {

        // Create a resizable thread pool
        ExecutorService imageExecutorService = new ThreadPoolExecutor(
                2,           // Core pool size
                8,                     // Maximum pool size
                60,                     // Keep-alive time for idle threads (seconds)
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>() // Unbounded queue for pending tasks
        );

        return imageExecutorService;
    }
}

