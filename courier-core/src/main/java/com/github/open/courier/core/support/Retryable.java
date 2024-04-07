package com.github.open.courier.core.support;

import com.github.rholder.retry.*;
import com.google.common.base.Predicate;

/**
 * 可重试的
 *
 * @author Courier
 */
public interface Retryable<R> extends Predicate<R>, RetryListener {

    /**
     * 默认遇到异常就重试, 不关心返回结果
     */
    @Override
    default boolean apply(R input) {
        return false;
    }

    /**
     * 默认每次重试完什么都不干
     */
    @Override
    default <V> void onRetry(Attempt<V> attempt) {
    }

    /**
     * 默认没有执行间隔
     */
    default AttemptTimeLimiter<R> getAttemptTimeLimiter() {
        return AttemptTimeLimiters.noTimeLimit();
    }

    /**
     * 默认永不停止重试
     */
    default StopStrategy getStopStrategy() {
        return StopStrategies.neverStop();
    }

    /**
     * 默认重试失败后不等待直接开始下一次重试
     */
    default WaitStrategy getWaitStrategy() {
        return WaitStrategies.noWait();
    }

    /**
     * 默认阻塞策略是Thread.sleep()
     */
    default BlockStrategy getBlockStrategy() {
        return BlockStrategies.threadSleepStrategy();
    }

    /**
     * 创建该重试策略对应的Retryer
     */
    default Retryer<R> build() {
        return RetryerBuilder
                .<R>newBuilder()
                .retryIfException()
                .retryIfResult(this)
                .withRetryListener(this)
                .withAttemptTimeLimiter(getAttemptTimeLimiter())
                .withStopStrategy(getStopStrategy())
                .withWaitStrategy(getWaitStrategy())
                .withBlockStrategy(getBlockStrategy())
                .build();
    }
}