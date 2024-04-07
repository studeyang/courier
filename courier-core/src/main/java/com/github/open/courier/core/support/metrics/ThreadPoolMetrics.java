package com.github.open.courier.core.support.metrics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

/**
 * 线程池指标, ThreadPoolExecutor不能直接序列化
 */
@Getter
public class ThreadPoolMetrics {

    final int corePoolSize;
    final int maximumPoolSize;
    final int poolSize;
    final int activeCount;
    final int queueSize;
    final long completedTaskCount;
    final long keepAliveSeconds;

    public ThreadPoolMetrics(ExecutorService executor) {
        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
        this.corePoolSize = pool.getCorePoolSize();
        this.maximumPoolSize = pool.getMaximumPoolSize();
        this.poolSize = pool.getPoolSize();
        this.activeCount = pool.getActiveCount();
        this.queueSize = pool.getQueue().size();
        this.keepAliveSeconds = pool.getKeepAliveTime(TimeUnit.SECONDS);
        this.completedTaskCount = pool.getCompletedTaskCount();
    }
}
