package com.github.open.courier.client.producer;

import com.github.open.courier.client.producer.sender.Sender;
import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.messaging.Message;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

/**
 * 默认的消息发布器, 消息将直接发送到Producer
 *
 * @author Courier
 */
@RequiredArgsConstructor
public class DefaultMessagePublisher implements MessagePublisher {

    private final Sender sender;

    @Override
    public void publish(Message message) {
        sender.trySend(SendMessageConverter.convert(message));
    }

    @Override
    public void publish(Collection<? extends Message> messages) {
        sender.trySend(SendMessageConverter.convert(messages));
    }
}
