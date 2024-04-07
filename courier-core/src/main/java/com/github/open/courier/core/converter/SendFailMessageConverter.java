package com.github.open.courier.core.converter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.open.courier.core.transport.SendMessage;
import org.apache.commons.collections4.CollectionUtils;

import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendFailMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 发送失败消息转换器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendFailMessageConverter {

    /**
     * 将一个SendMessage转换为SendFailMessage
     */
    public static SendFailMessage toFailMessage(SendMessage message, MessageSendResult result) {

        return new SendFailMessage()
                .setMessageId(message.getMessageId())
                .setTopic(message.getTopic())
                .setType(message.getType())
                .setService(message.getService())
                .setContent(message.getContent())
                .setCreatedAt(message.getCreatedAt())
                .setPrimaryKey(message.getPrimaryKey())
                .setUsage(message.getUsage())
                .setRetries(message.getRetries())
                .setReason(result == null ? null : result.getReason());
    }

    /**
     * 将一批SendMessage转换为发送失败消息
     */
    public static List<SendFailMessage> toFailMessages(List<SendMessage> messages, List<MessageSendResult> results) {

        Map<String, MessageSendResult> reasonMap = CollectionUtils.isEmpty(results)
                ? Collections.emptyMap()
                : results.stream().collect(Collectors.toMap(MessageSendResult::getMessageId, Function.identity()));

        return messages.stream()
                .map(m -> toFailMessage(m, reasonMap.get(m.getMessageId())))
                .collect(Collectors.toList());
    }


}
