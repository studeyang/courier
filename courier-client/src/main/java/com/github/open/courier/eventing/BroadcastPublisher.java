package com.github.open.courier.eventing;

import com.github.open.courier.client.producer.MessagePublisher;
import com.github.open.courier.core.exception.NotDataSourceException;
import com.github.open.courier.core.message.Broadcast;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 广播发布器
 *
 * @author Courier
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastPublisher {

    @Setter
    private static MessagePublisher publisher;
    @Setter
    private static MessagePublisher transactionPublisher;
    @Setter
    private static MessagePublisher delayPublisher;

    /**
     * 发布一个广播
     */
    public static void publish(Broadcast broadcast) {
        publisher.publish(broadcast);
    }

    /**
     * 发布一批广播
     */
    public static void publish(Collection<? extends Broadcast> broadcasts) {
        publisher.publish(broadcasts);
    }

    /**
     * 发布一个延迟事件
     *
     * @param broadcast 广播
     * @param delay     延迟时间
     * @param timeUnit  时间单位
     */
    public static void publish(Broadcast broadcast, Long delay, TimeUnit timeUnit) {
        delayPublisher.publish(broadcast, delay, timeUnit);
    }

    /**
     * 发布一批延迟事件
     *
     * @param broadcasts 广播
     * @param delay      延迟时间
     * @param timeUnit   时间单位
     */
    public static void publish(Collection<? extends Broadcast> broadcasts, Long delay, TimeUnit timeUnit) {
        delayPublisher.publish(broadcasts, delay, timeUnit);
    }

    /**
     * 发布一个事务广播, 在本地事务提交后再发送到Kafka
     */
    public static void publishTransaction(Broadcast broadcasts) {
        checkDataSource();
        transactionPublisher.publish(broadcasts);
    }

    /**
     * 发布一批事务广播, 在本地事务提交后再发送到Kafka
     */
    public static void publishTransaction(Collection<? extends Broadcast> broadcasts) {
        checkDataSource();
        transactionPublisher.publish(broadcasts);
    }

    private static void checkDataSource() {
        if (transactionPublisher == null) {
            throw new NotDataSourceException("当前服务无数据源, 无法推送事务消息");
        }
    }
}