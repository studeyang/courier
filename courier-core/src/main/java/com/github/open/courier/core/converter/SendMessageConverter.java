package com.github.open.courier.core.converter;

import com.github.open.courier.core.message.Usage;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.support.CourierVersion;
import com.github.open.courier.core.support.id.MessageId;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.exception.NullMessageException;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.messaging.Message;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 发送消息转换器
 *
 * @author Courier
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMessageConverter {

    /**
     * Message转换为SendMessage
     *
     * @param message 消息
     */
    public static SendMessage convert(Message message) {
        return convert(message, new Date(), null);
    }

    /**
     * Message转换为SendMessage
     *
     * @param message     消息
     * @param delayMillis 延迟时间（ms）
     */
    public static SendMessage convert(Message message, Long delayMillis) {
        return convert(message, new Date(), delayMillis);
    }

    /**
     * Message转换为SendMessage
     *
     * @param messages 一批消息
     */
    public static List<SendMessage> convert(Collection<? extends Message> messages) {
        return convert(messages, null);
    }

    /**
     * Message转换为SendMessage
     *
     * @param messages    一批消息
     * @param delayMillis 延迟时间（ms）
     */
    public static List<SendMessage> convert(Collection<? extends Message> messages, Long delayMillis) {
        Date now = new Date();
        return messages.stream().map(m -> convert(m, now, delayMillis)).collect(Collectors.toList());
    }

    /**
     * 将消息按发送结果分类
     *
     * @param messages    消息
     * @param sendResults 消息发送结果
     * @return 结果成功/失败分类, key: false(失败)
     */
    public static Map<Boolean, List<SendMessage>> classify(List<SendMessage> messages,
                                                           List<MessageSendResult> sendResults) {

        Set<String> successIds = sendResults.stream()
                .filter(MessageSendResult::isSuccess)
                .map(MessageSendResult::getMessageId)
                .collect(Collectors.toSet());

        return messages.stream()
                .collect(Collectors.partitioningBy(m -> successIds.contains(m.getMessageId())));
    }

    private static SendMessage convert(Message message, Date date, Long delayMillis) {

        if (message == null) {
            throw new NullMessageException("推送的消息不能为空");
        }

        Class<?> clazz = message.getClass();

        message.setId(MessageId.getId())
                .setPrimaryKey(MessageIntrospector.getPrimaryKey(message))
                .setUsage(Usage.of(message))
                .setService(CourierContext.getService())
                .setTopic(MessageIntrospector.getTopic(clazz))
                .setTimeStamp(date)
                .setClientVersion(CourierVersion.get());

        return new SendMessage()
                .setMessageId(message.getId())
                .setTopic(message.getTopic())
                .setType(clazz.getName())
                .setService(message.getService())
                .setContent(MessageJsonConverter.toJson(message))
                .setCreatedAt(message.getTimeStamp())
                .setPrimaryKey(message.getPrimaryKey())
                .setUsage(message.getUsage())
                .setRetries(0)
                .setDelayMillis(delayMillis);
    }
}
