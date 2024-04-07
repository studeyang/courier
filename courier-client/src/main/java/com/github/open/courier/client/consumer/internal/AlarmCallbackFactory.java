package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.client.feign.ConsumerClient;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.support.MessageTemplate;
import com.github.open.courier.core.support.Retryable;
import com.github.open.courier.core.support.executor.PausableProperties.*;
import com.github.open.courier.core.support.executor.AlarmCallback;
import com.github.open.courier.core.transport.ThreadPoolAlarmMetadata;
import com.github.rholder.retry.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 消费告警器
 */
@Slf4j
public class AlarmCallbackFactory {


    public static AlarmCallback newInstance(AlarmProperties alarmProperties, ConsumerClient consumerClient) {

        switch (alarmProperties.getType()) {
            case SYNC:
                return new SyncCallback(consumerClient);
            case ASYNC:
                return new AsyncCallback(consumerClient, alarmProperties.getAsync().create());
            default:
                return AlarmCallback.DEFAULT;
        }
    }

    /**
     * 【同步】告警回调, 提交任务前同步执行
     */
    @RequiredArgsConstructor
    static class SyncCallback implements AlarmCallback {

        final ConsumerClient consumerClient;

        @Override
        public void alarm(ThreadPoolAlarmMetadata metadata) {
            handleAlarm(consumerClient, metadata);
        }

        @Override
        public void recovery(ThreadPoolAlarmMetadata metadata) {
            handleRecovery(consumerClient, metadata);
        }
    }


    /**
     * 【异步】告警回调, 提交任务前将告警任务提交到线程池
     */
    @RequiredArgsConstructor
    static class AsyncCallback  implements AlarmCallback {

        final ConsumerClient consumerClient;
        final ExecutorService executorService;

        @Override
        public void alarm(ThreadPoolAlarmMetadata metadata) {
            executorService.execute(() -> handleAlarm(consumerClient, metadata));
        }

        @Override
        public void recovery(ThreadPoolAlarmMetadata metadata) {
            executorService.execute(() -> handleRecovery(consumerClient, metadata));
        }
    }


    static void handleAlarm(ConsumerClient consumerClient, ThreadPoolAlarmMetadata metadata) {

        String warnMsg = MessageTemplate.handleThreadPoolWarnText(metadata);

        Runnable warnTask = () -> consumerClient.alarm(CourierContext.getService(), warnMsg);

        try {
            RetryerHolder.retryer.call(Executors.callable(warnTask));
        } catch (Exception e) {
            log.error("线程池告警消息发送失败", e);
        }
    }


    static void handleRecovery(ConsumerClient consumerClient, ThreadPoolAlarmMetadata metadata) {

        String recoveMsg = MessageTemplate.handleThreadPoolRecoverText(metadata);

        Runnable revoceTask = () -> consumerClient.recovery(CourierContext.getService(), recoveMsg);

        try {
            RetryerHolder.retryer.call(Executors.callable(revoceTask));
        } catch (Exception e) {
            log.error("线程池告警恢复发送失败", e);
        }
    }


    enum RetryerHolder implements Retryable<Object> {

        INSTANCE;

        static final Retryer<Object> retryer = INSTANCE.build();

        @Override
        public WaitStrategy getWaitStrategy() {
            // 200ms 200ms 200ms 200ms 200ms 200ms ...
            return WaitStrategies.fixedWait(200, TimeUnit.MILLISECONDS);
        }

        @Override
        public StopStrategy getStopStrategy() {
            return StopStrategies.stopAfterAttempt(3);
        }
    }


}
