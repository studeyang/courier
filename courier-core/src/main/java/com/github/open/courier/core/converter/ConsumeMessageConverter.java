package com.github.open.courier.core.converter;

import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.core.message.Usage;
import com.github.open.courier.core.support.id.MessageId;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.github.open.courier.core.constant.MessageConstant.*;

/**
 * ConsumeMessage转换器
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumeMessageConverter {

    /**
     * 过滤所需的ConsumerRecord, 并转换为ConsumeMessage
     */
    public static List<ConsumeMessage> toConsumeMessages(Iterable<ConsumerRecord<String, String>> records,
                                                         Set<String> types,
                                                         String service,
                                                         String groupId) {
        Date pollTime = new Date();
        List<ConsumeMessage> messages = Lists.newArrayList();
        for (ConsumerRecord<String, String> record : records) {
            ConsumeMessage message = toConsumeMessage(record, pollTime, types, service, groupId);
            if (message != null) {
                messages.add(message);
            }
        }
        return messages;
    }

    public static ConsumeMessage toConsumeMessage(ConsumerRecord<String, String> record,
                                                  Date pollTime,
                                                  Set<String> types,
                                                  String service,
                                                  String groupId) {

        JsonNode node = MessageJsonConverter.toNode(record.value());

        if (node == null) {
            log.error("kafka消息不是json格式, record:{}", record);
            return null;
        }

        String type = asText(node.get(MESSAGE_TYPE));

        // 如果不是该listener所需的type, 过滤掉
        if (StringUtils.isEmpty(type) || !types.contains(type)) {
            return null;
        }

        String cluster = null;
        String env = null;
        for (Header header : record.headers()) {
            if (CLUSTER.equals(header.key())) {
                cluster = new String(header.value());
            } else if (ENV.equals(header.key())) {
                env = new String(header.value());
            }
        }

        return new ConsumeMessage()
                .setId(MessageId.getId())
                .setMessageId(asText(node.get("id")))
                .setTopic(record.topic())
                .setType(type)
                .setGroupId(groupId)
                .setFromService(asText(node.get("service")))
                .setToService(service)
                .setContent(record.value())
                .setCreatedAt(new Date(record.timestamp()))
                .setPrimaryKey(asText(node.get("primaryKey")))
                // messagebus消息, usage为null, 默认为Event
                .setUsage(Usage.of(asText(node.get("usage"))))
                .setRetries(0)
                .setNeedRepush(true)
                .setPollTime(pollTime)
                .setFromCluster(cluster)
                .setFromEnv(env);
    }

    private static String asText(JsonNode node) {
        return node == null || node.isNull() ? null : node.asText();
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    public static String getSql(List<ConsumeMessage> messages, boolean usePollTimeAsPushTime) {

        String pollTime = formatter.format(messages.get(0).getPollTime().toInstant());

        StringBuilder sql = new StringBuilder(512);
        for (ConsumeMessage message : messages) {
            sql.append("('")
                    .append(message.getId())
                    .append("','")
                    .append(message.getMessageId())
                    .append("','")
                    .append(StringUtils.defaultString(message.getFromService()))
                    .append("','")
                    .append(message.getToService())
                    .append("','")
                    .append(message.getTopic())
                    .append("','")
                    .append(message.getGroupId())
                    .append("',")
                    .append(message.getRetries())
                    .append(",'COMMITED',")
                    .append(message.getNeedRepush())
                    .append(",'")
                    .append(pollTime);

            if (usePollTimeAsPushTime) {
                sql.append("','")
                        .append(pollTime)
                        .append("','")
                        .append(pollTime)
                        .append("',NULL,NULL,0),");
            } else {
                sql.append("',NULL,NULL,NULL,NULL,0),");
            }

        }
        return sql.substring(0, sql.length() - 1);
    }
}
