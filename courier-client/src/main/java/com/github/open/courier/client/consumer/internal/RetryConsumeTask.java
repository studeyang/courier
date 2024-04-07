package com.github.open.courier.client.consumer.internal;

import static com.github.open.courier.core.converter.ExceptionConverter.getCause;

import java.util.concurrent.TimeUnit;

import com.github.open.courier.core.support.Retryable;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.StopStrategy;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 重试消费task
 */
@Slf4j
public class RetryConsumeTask extends ConsumeTask {

    public RetryConsumeTask(ConsumeTask task) {
        super(task.consumeSupport, task.consumeMessage, task.receiveStamp);
        super.message = task.message;
        super.handler = task.handler;
        super.beforeConsumeStamp = task.beforeConsumeStamp;
        super.afterConsumeStamp = task.afterConsumeStamp;
    }

    /**
     * 重试消费
     */
    @Override
    public void run() {

        Exception fail = null;
        try {
            RetryerHolder.retryer.call(() -> {
                doConsume();
                return null;
            });
        } catch (Exception e) {
            fail = e;
        }

        if (fail == null) {
            consumeSupport.success(this);
        } else {
            consumeSupport.fail(consumeMessage.setRetries(3), getCause(fail), false);
        }
    }

    @Override
    public boolean isRetry() {
        return true;
    }

    enum RetryerHolder implements Retryable<Object> {

        INSTANCE;

        static final Retryer<Object> retryer = INSTANCE.build();

        @Override
        public <V> void onRetry(Attempt<V> attempt) {
            if (attempt.hasException()) {
                log.warn("kafka第{}次重试消费失败", attempt.getAttemptNumber(), attempt.getExceptionCause());
            }
        }

        /**
         * 重试间隔, 1s, 2s, 3s
         */
        @Override
        public WaitStrategy getWaitStrategy() {
            return WaitStrategies.incrementingWait(1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
        }

        @Override
        public StopStrategy getStopStrategy() {
            return StopStrategies.stopAfterAttempt(3);
        }
    }
}
