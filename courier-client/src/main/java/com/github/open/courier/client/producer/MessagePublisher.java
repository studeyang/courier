package com.github.open.courier.client.producer;

import com.github.open.courier.messaging.Message;
import org.springframework.scheduling.annotation.Async;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.github.open.courier.core.constant.MessageConstant.PRODUCER_EXECUTOR_BEAN;

/**
 * 消息发布器
 *
 * @author Courier
 */
@Async(PRODUCER_EXECUTOR_BEAN)
public interface MessagePublisher {

    /**
     * 发布一个消息
     *
     * @param message 消息
     */
    void publish(Message message);

    /**
     * 发布一批消息
     *
     * @param messages 消息集合
     */
    void publish(Collection<? extends Message> messages);

    /**
     * 发布一个延迟消息
     *
     * @param message  消息
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    default void publish(Message message, Long delay, TimeUnit timeUnit) {
    }

    /**
     * 发布一批延迟消息
     *
     * @param messages 一批消息
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    default void publish(Collection<? extends Message> messages, Long delay, TimeUnit timeUnit) {
    }

}
