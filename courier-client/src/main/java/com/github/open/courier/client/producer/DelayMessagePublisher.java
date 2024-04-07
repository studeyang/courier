package com.github.open.courier.client.producer;

import com.github.open.courier.client.producer.sender.Sender;
import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * 延迟消息发布器
 *
 * @author yanglulu
 */
@Slf4j
@RequiredArgsConstructor
public class DelayMessagePublisher implements MessagePublisher {

    private final Sender sender;

    @Override
    public void publish(Message message) {
        throw new UnsupportedOperationException("Please use DefaultMessagePublisher # publish(Message.class)");
    }

    @Override
    public void publish(Collection<? extends Message> messages) {
        throw new UnsupportedOperationException("Please use DefaultMessagePublisher # publish(Collection.class)");
    }

    /**
     * 发布一个延迟消息
     *
     * @param message  消息
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    @Override
    public void publish(Message message, Long delay, TimeUnit timeUnit) {
        sender.trySend(SendMessageConverter.convert(message, timeUnit.toMillis(delay)));
    }

    /**
     * 发布一批延迟消息
     *
     * @param messages 一批消息
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     */
    @Override
    public void publish(Collection<? extends Message> messages, Long delay, TimeUnit timeUnit) {
        sender.trySend(SendMessageConverter.convert(messages, timeUnit.toMillis(delay)));
    }

}
