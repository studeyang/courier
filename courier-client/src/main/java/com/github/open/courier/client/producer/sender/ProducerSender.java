package com.github.open.courier.client.producer.sender;

import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.converter.ExceptionConverter;
import com.github.open.courier.core.transport.MessageSendResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yanglulu
 */
@Slf4j
@RequiredArgsConstructor
public class ProducerSender implements Sender {

    private static final String SEND_ERROR = "kafka发送消息失败, cause:{}";

    private final ProducerClient producerClient;
    /**
     * 每批发送的集合大小
     */
    @Getter
    private final int partitionSize;

    @Override
    public MessageSendResult trySend(SendMessage message) {

        log.debug("send to producer.");

        try {
            return producerClient.send(message);
        } catch (Exception e) {
            String cause = ExceptionConverter.getCause(e);
            log.warn(SEND_ERROR, cause);
            return MessageSendResult.error(message.getMessageId(), cause);
        }
    }

    @Override
    public List<MessageSendResult> trySend(List<SendMessage> messages) {

        log.debug("send to producer.");

        return messages.size() <= partitionSize
                ? doSendBatch(messages)
                : ListUtils.partition(messages, partitionSize)
                .stream()
                .map(this::doSendBatch)
                .collect(ArrayList::new, List::addAll, List::addAll);
    }

    private List<MessageSendResult> doSendBatch(List<SendMessage> messages) {

        try {
            return producerClient.send(messages);
        } catch (Exception e) {
            String cause = ExceptionConverter.getCause(e);
            log.warn(SEND_ERROR, cause);
            return messages.stream()
                    .map(m -> MessageSendResult.error(m.getMessageId(), cause))
                    .collect(Collectors.toList());
        }
    }

}
