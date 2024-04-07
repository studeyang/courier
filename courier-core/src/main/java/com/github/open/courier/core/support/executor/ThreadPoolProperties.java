package com.github.open.courier.core.support.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * 线程池参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class ThreadPoolProperties {

    /**
     * 核心线程数
     */
    private int corePoolSize;

    /**
     * 最大线程数
     */
    private int maxPoolSize;

    /**
     * 超时时间(秒)
     */
    private int keepAliveSeconds;

    /**
     * 队列容量
     */
    private int queueCapacity;

    /**
     * 拒绝策略(全限定名)
     */
    private Class<? extends RejectedExecutionHandler> rejectedHandler;

    /**
     * 线程名前缀
     */
    private String threadNamePrefix;

    public ExecutorService create() {
        return new ThreadPoolExecutor(corePoolSize,
                                      maxPoolSize,
                                      keepAliveSeconds, TimeUnit.SECONDS,
                                      new LinkedBlockingQueue<>(queueCapacity),
                                      new CustomizableThreadFactory(threadNamePrefix),
                                      instanceHandler());
    }

    @SneakyThrows
    public RejectedExecutionHandler instanceHandler() {
        return rejectedHandler.newInstance();
    }
}
