package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.client.feign.ManagementClient;
import com.github.open.courier.client.consumer.internal.CourierClientProperties.ConsumerProperties.ReporterProperties;
import com.github.open.courier.client.consumer.internal.CourierClientProperties.ConsumerProperties.ReporterProperties.BufferProperties;
import com.github.open.courier.client.consumer.internal.CourierClientProperties.ConsumerProperties.ReporterProperties.BufferProperties.TimedBufferProperties;
import com.github.open.courier.core.support.Retryable;
import com.github.open.courier.core.support.TimedBuffer;
import com.github.open.courier.core.support.TimedBuffer.RejectedStrategy.RejectedStrategies;
import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.core.transport.MessageConsumeTime;
import com.github.rholder.retry.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 消费结束后发送消费结果
 */
@Slf4j
public class ConsumeReporters {

    /**
     * Reporter
     */
    public interface ConsumeReporter {

        /**
         * 报告消费成功
         */
        void reportSuccess(MessageConsumeTime time);

        /**
         * 报告消费失败
         */
        void reportFail(ConsumeFailMessage message);
    }

    /**
     * 实例化不同Reporter
     */
    public static ConsumeReporter newInstance(ReporterProperties properties, ManagementClient client) {
        switch (properties.getType()) {
            case SYNC:
                return new SyncReporter(client);
            case ASYNC:
                return new AsyncReporter(client, properties.getAsync().create());
            case BUFFER:
                return new BufferReporter(client, properties.getBuffer());
            default:
                throw new IllegalArgumentException("reporter type不能为null");
        }
    }

    /**
     * 同步Reporter, 消费结束后直接报告
     */
    @RequiredArgsConstructor
    protected static class SyncReporter implements ConsumeReporter {

        final ManagementClient client;

        @Override
        public void reportSuccess(MessageConsumeTime time) {
            report(() -> client.handleSuccess(Collections.singletonList(time)));
        }

        @Override
        public void reportFail(ConsumeFailMessage message) {
            report(() -> client.handleFail(Collections.singletonList(message)));
        }
    }

    /**
     * 异步Reporter, 交给线程池异步报告
     */
    @RequiredArgsConstructor
    protected static class AsyncReporter implements ConsumeReporter {

        final ManagementClient client;
        @Getter
        final ExecutorService executor;

        @Override
        public void reportSuccess(MessageConsumeTime time) {
            executor.execute(() -> report(() -> client.handleSuccess(Collections.singletonList(time))));
        }

        @Override
        public void reportFail(ConsumeFailMessage message) {
            executor.execute(() -> report(() -> client.handleFail(Collections.singletonList(message))));
        }

        @PreDestroy
        public void close() {
            executor.shutdown();
            log.info("AsyncReporter closed");
        }
    }

    /**
     * 定时缓冲Reporter, 交给TimedBuffer异步缓冲报告
     */
    @Getter
    protected static class BufferReporter implements ConsumeReporter {

        final TimedBuffer<MessageConsumeTime> successBuffer;
        final TimedBuffer<ConsumeFailMessage> failBuffer;

        public BufferReporter(ManagementClient client, BufferProperties properties) {

            TimedBufferProperties success = properties.getSuccess();
            TimedBufferProperties fail = properties.getFail();

            this.successBuffer = new TimedBuffer<>(success.getBufferSize(),
                    success.getTimeoutSeconds(), TimeUnit.SECONDS,
                    buffer -> report(() -> client.handleSuccess(buffer)),
                    success.getCoreConsumerSize(),
                    new LinkedBlockingQueue<>(success.getQueueCapacity()),
                    RejectedStrategies.RUN,
                    new CustomizableThreadFactory(success.getThreadNamePrefix()));

            this.failBuffer = new TimedBuffer<>(fail.getBufferSize(),
                    fail.getTimeoutSeconds(), TimeUnit.SECONDS,
                    buffer -> report(() -> client.handleFail(buffer)),
                    fail.getCoreConsumerSize(),
                    new LinkedBlockingQueue<>(fail.getQueueCapacity()),
                    RejectedStrategies.RUN,
                    new CustomizableThreadFactory(fail.getThreadNamePrefix()));
        }

        @Override
        public void reportSuccess(MessageConsumeTime time) {
            successBuffer.offer(time);
        }

        @Override
        public void reportFail(ConsumeFailMessage message) {
            failBuffer.offer(message);
        }

        @PreDestroy
        public void close() {
            successBuffer.close();
            failBuffer.close();
            log.info("BufferReporter closed");
        }
    }

    /**
     * 重试报告
     */
    static void report(Runnable reportTask) {
        try {
            RetryerHolder.retryer.call(Executors.callable(reportTask));
            log.debug("kafka发送消费结果成功");
        } catch (Exception e) {
            log.error("kafka发送消费结果失败", e);
        }
    }

    enum RetryerHolder implements Retryable<Object> {

        INSTANCE;

        static final Retryer<Object> retryer = INSTANCE.build();

        @Override
        public WaitStrategy getWaitStrategy() {
            // 1s 2s 3s 4s 5s 6s ...
            return WaitStrategies.incrementingWait(1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS);
        }

        @Override
        public StopStrategy getStopStrategy() {
            return StopStrategies.stopAfterAttempt(3);
        }
    }
}
