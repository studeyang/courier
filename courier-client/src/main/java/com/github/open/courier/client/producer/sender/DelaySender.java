package com.github.open.courier.client.producer.sender;

import com.github.open.courier.client.feign.DelayClient;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.converter.ExceptionConverter;
import com.github.open.courier.core.transport.MessageSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 延迟消息发送
 *
 * @author yanglulu
 */
@Slf4j
@RequiredArgsConstructor
public class DelaySender implements Sender {

    private static final String SEND_ERROR = "kafka发送延迟消息失败, cause:{}";

    private final DelayClient delayClient;
    private final int partitionSize;

    @Override
    public MessageSendResult trySend(SendMessage message) {

        log.debug("send to delay.");

        try {
            return delayClient.send(message);
        } catch (Exception e) {
            String cause = ExceptionConverter.getCause(e);
            log.warn(SEND_ERROR, cause);
            return MessageSendResult.error(message.getMessageId(), cause);
        }
    }

    @Override
    public List<MessageSendResult> trySend(List<SendMessage> messages) {

        log.debug("send to delay.");

        return messages.size() <= partitionSize
                ? doSendBatch(messages)
                : ListUtils.partition(messages, partitionSize)
                .stream()
                .map(this::doSendBatch)
                .collect(ArrayList::new, List::addAll, List::addAll);
    }

    private List<MessageSendResult> doSendBatch(List<SendMessage> messages) {

        try {
            return delayClient.send(messages);
        } catch (Exception e) {
            String cause = ExceptionConverter.getCause(e);
            log.warn(SEND_ERROR, cause);
            return messages.stream()
                    .map(m -> MessageSendResult.error(m.getMessageId(), cause))
                    .collect(Collectors.toList());
        }
    }

}
