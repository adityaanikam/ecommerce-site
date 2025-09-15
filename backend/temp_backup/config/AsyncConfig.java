package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async Configuration for Background Processing
 * 
 * This configuration sets up thread pools for asynchronous processing
 * of heavy operations like email sending, image processing, and analytics.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Task Executor for general async operations
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - minimum number of threads
        executor.setCorePoolSize(5);
        
        // Maximum pool size - maximum number of threads
        executor.setMaxPoolSize(20);
        
        // Queue capacity - number of tasks that can be queued
        executor.setQueueCapacity(100);
        
        // Thread name prefix for debugging
        executor.setThreadNamePrefix("Async-");
        
        // Keep alive time for idle threads
        executor.setKeepAliveSeconds(60);
        
        // Rejection policy when queue is full
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Maximum time to wait for tasks to complete
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Email Task Executor for email operations
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Smaller pool for email operations
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Email-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Analytics Task Executor for heavy analytics operations
     */
    @Bean(name = "analyticsTaskExecutor")
    public Executor analyticsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Dedicated pool for analytics
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Analytics-");
        executor.setKeepAliveSeconds(120);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }

    /**
     * Image Processing Task Executor
     */
    @Bean(name = "imageTaskExecutor")
    public Executor imageTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Pool for image processing operations
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(30);
        executor.setThreadNamePrefix("Image-");
        executor.setKeepAliveSeconds(90);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(45);
        
        executor.initialize();
        return executor;
    }

    /**
     * Notification Task Executor
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Pool for notification operations
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(40);
        executor.setThreadNamePrefix("Notification-");
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        return executor;
    }

    /**
     * Task Scheduler for scheduled tasks
     */
    @Bean(name = "taskScheduler")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        // Pool size for scheduled tasks
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("Scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Async Service for managing async operations
     */
    @Bean
    public AsyncService asyncService() {
        return new AsyncService();
    }

    /**
     * Async Service implementation
     */
    public static class AsyncService {
        private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AsyncService.class);

        /**
         * Execute task asynchronously with error handling
         */
        public void executeAsync(Runnable task, String taskName) {
            try {
                task.run();
                logger.debug("Async task completed: {}", taskName);
            } catch (Exception e) {
                logger.error("Async task failed: {}", taskName, e);
            }
        }

        /**
         * Execute task with retry logic
         */
        public void executeWithRetry(Runnable task, String taskName, int maxRetries) {
            int attempts = 0;
            while (attempts < maxRetries) {
                try {
                    task.run();
                    logger.debug("Async task completed: {} (attempt {})", taskName, attempts + 1);
                    return;
                } catch (Exception e) {
                    attempts++;
                    if (attempts >= maxRetries) {
                        logger.error("Async task failed after {} attempts: {}", maxRetries, taskName, e);
                        throw e;
                    }
                    logger.warn("Async task failed (attempt {}): {}, retrying...", attempts, taskName, e.getMessage());
                    
                    // Exponential backoff
                    try {
                        Thread.sleep(1000 * (long) Math.pow(2, attempts - 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Task interrupted", ie);
                    }
                }
            }
        }
    }
}
