package com.github.open.courier.eventing;

import com.github.open.courier.client.producer.MessagePublisher;
import com.github.open.courier.core.exception.NotDataSourceException;
import com.github.open.courier.messaging.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 事件发布器
 *
 * @author Courier
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventPublisher {

    @Setter
    private static MessagePublisher publisher;
    @Setter
    private static MessagePublisher transactionPublisher;
    @Setter
    private static MessagePublisher delayPublisher;

    /**
     * 发布一个事件
     */
    public static void publish(Event event) {
        publisher.publish(event);
    }

    /**
     * 发布一批事件
     */
    public static void publish(Collection<? extends Event> events) {
        publisher.publish(events);
    }

    /**
     * 发布一个延迟事件
     *
     * @param event    事件
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    public static void publish(Event event, Long delay, TimeUnit timeUnit) {
        delayPublisher.publish(event, delay, timeUnit);
    }

    /**
     * 发布一批延迟事件
     *
     * @param events   事件
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    public static void publish(Collection<? extends Event> events, Long delay, TimeUnit timeUnit) {
        delayPublisher.publish(events, delay, timeUnit);
    }

    /**
     * 发布一个事务事件, 在本地事务提交后再发送到Kafka
     */
    public static void publishTransaction(Event event) {
        checkDataSource();
        transactionPublisher.publish(event);
    }

    /**
     * 发布一批事务事件, 在本地事务提交后再发送到Kafka
     */
    public static void publishTransaction(Collection<? extends Event> events) {
        checkDataSource();
        transactionPublisher.publish(events);
    }

    private static void checkDataSource() {
        if (transactionPublisher == null) {
            throw new NotDataSourceException("当前服务无数据源, 无法推送事务消息");
        }
    }
}