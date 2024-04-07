package com.github.open.courier.core.converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.core.transport.ConsumeFailMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 消费失败消息转换器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumeFailMessageConverter {

    /**
     * 转换一批失败消息
     */
    public static List<ConsumeFailMessage> toConsumeFailMessages(Collection<ConsumeMessage> messages, String cause) {

        return messages.stream()
                .map(m -> toConsumeFailMessage(m, cause))
                .collect(Collectors.toList());
    }

    /**
     * 转换一条失败消息
     */
    public static ConsumeFailMessage toConsumeFailMessage(ConsumeMessage message, String reason) {

        return new ConsumeFailMessage()
                .setId(message.getId())
                .setMessageId(message.getMessageId())
                .setTopic(message.getTopic())
                .setType(message.getType())
                .setGroupId(message.getGroupId())
                .setService(message.getToService())
                .setCreatedAt(message.getCreatedAt())
                .setPrimaryKey(message.getPrimaryKey())
                .setUsage(message.getUsage())
                .setRetries(message.getRetries())
                .setReason(reason)
                .setNeedRepush(message.getNeedRepush())
                .setPollTime(message.getPollTime());
    }

}
