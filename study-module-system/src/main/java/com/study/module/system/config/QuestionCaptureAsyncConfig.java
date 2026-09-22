package com.study.module.system.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 题目采集异步任务线程池配置。
 */
@Configuration
public class QuestionCaptureAsyncConfig {

    /**
     * 创建与 Web 请求线程隔离的题目采集执行器，并注册线程池指标。
     */
    @Bean("questionCaptureExecutor")
    public Executor questionCaptureExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("question-capture-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        // 队列满时保留 CallerRuns 的降级行为，确保 OCR 不可用/积压时不丢任务；
        // 同时记录拒绝次数，作为积压告警的直接信号。
        executor.setRejectedExecutionHandler((task, threadPool) -> {
            meterRegistry.counter("question.capture.executor.rejected").increment();
            new ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(task, threadPool);
        });
        executor.initialize();
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "question.capture.executor");
        return executor;
    }
}
