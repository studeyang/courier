package com.github.open.courier.core.converter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.DBMessage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * ConsumeMessage转换器
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BackupMessageConverter {

    public static List<DBMessage> toDBMessages(List<SendMessage> messages, Map<String, RecordMetadata> recordMetadatas) {

        return messages.stream()
                .map(message -> toDBMessage(message, recordMetadatas.get(message.getMessageId())))
                .collect(Collectors.toList());
    }

    public static DBMessage toDBMessage(SendMessage message, RecordMetadata recordMetadata) {

        String fullType = message.getType();

        return new DBMessage()
                .setMessageId(message.getMessageId())
                .setTopic(message.getTopic())
                .setType(fullType)
                .setEvent(fullType == null ? "" : fullType.substring(fullType.lastIndexOf('.') + 1))
                .setFromService(message.getService())
                .setContent(message.getContent())
                .setCreatedAt(message.getCreatedAt())
                .setPrimaryKey(message.getPrimaryKey())
                .setUsage(message.getUsage())
                .setOffset(recordMetadata.offset())
                .setPartition(recordMetadata.partition())
                .setCluster(message.getCluster())
                .setEnv(message.getEnv());
    }
}
