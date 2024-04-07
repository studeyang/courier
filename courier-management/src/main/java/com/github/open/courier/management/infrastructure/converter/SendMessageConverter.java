package com.github.open.courier.management.infrastructure.converter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.DBMessage;
import com.github.open.courier.core.transport.SendFailMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 发送消息转换器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendMessageConverter {

    public static List<SendMessage> toSendMessage(List<SendFailMessage> failMessages, List<DBMessage> dbMessages) {
        return Stream.concat(failMessages.stream().map(SendMessageConverter::toSendMessageByFail),
                             dbMessages.stream().map(SendMessageConverter::toSendMessagesByDB))
                .filter(distinctByKey(SendMessage::getMessageId))
                .collect(Collectors.toList());
    }
    
    /**
     * 将一个SendFailMessage转换为SendMessage
     */
    private static SendMessage toSendMessageByFail(SendFailMessage failMessage) {

        return new SendMessage()
                .setMessageId(failMessage.getMessageId())
                .setTopic(failMessage.getTopic())
                .setType(failMessage.getType())
                .setService(failMessage.getService())
                .setContent(failMessage.getContent())
                .setCreatedAt(failMessage.getCreatedAt())
                .setUsage(failMessage.getUsage())
                .setPrimaryKey(failMessage.getPrimaryKey())
                .setRetries(failMessage.getRetries())
                .setCluster(failMessage.getCluster())
                .setEnv(failMessage.getEnv());
    }

    private static SendMessage toSendMessagesByDB(DBMessage dbMessage) {

        return new SendMessage()
                .setMessageId(dbMessage.getMessageId())
                .setTopic(dbMessage.getTopic())
                .setType(dbMessage.getType())
                .setService(dbMessage.getFromService())
                .setContent(dbMessage.getContent())
                .setCreatedAt(dbMessage.getCreatedAt())
                .setUsage(dbMessage.getUsage())
                .setPrimaryKey(dbMessage.getPrimaryKey())
                .setRetries(0)
                .setCluster(dbMessage.getCluster())
                .setEnv(dbMessage.getEnv());
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {

        Set<Object> set = ConcurrentHashMap.newKeySet();

        return t -> set.add(keyExtractor.apply(t));
    }
}
