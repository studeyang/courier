package com.github.open.courier.management.infrastructure.converter;

import com.alibaba.nacos.api.naming.NamingService;
import com.github.open.courier.commons.loadbalance.LoadBalancer;
import com.github.open.courier.commons.support.ServiceRegistration;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.MessageQueryConditionConverter;
import com.github.open.courier.core.message.Usage;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.management.service.support.NamingServiceHolder;
import com.github.open.courier.repository.biz.SubscribeBizService;
import com.github.open.courier.repository.mapper.MessageMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 消费消息转换器
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumeMessageConverter {

    /**
     * 将ConsumeFailMessage转换为ConsumeMessage
     *
     * @return 例如：{@code http://10.0.0.1:11116/courier/messages/receive} -> [...]
     */
    public static Map<String, List<ConsumeMessage>> toConsumeMessagesByFail(String toCluster,
                                                                            List<ConsumeFailMessage> fails) {

        // 查询dbMessage
        Map<String, DBMessage> dbMap = CourierContext.getBean(MessageMapper.class)
                .listByMessageIds(MessageQueryConditionConverter.toMessageQueryConditionByFail(fails))
                .stream()
                .collect(Collectors.toMap(DBMessage::getMessageId, Function.identity()));

        // 查询url
        Set<String> services = fails.stream().map(ConsumeFailMessage::getService).collect(Collectors.toSet());
        // 例如：example-consumer -> http://10.0.0.1:11116/courier/messages/receive
        Map<String, String> serviceUrl = CourierContext.getBean(SubscribeBizService.class).listByServices(toCluster, services)
                .stream()
                .collect(
                        Collectors.toMap(
                                SubscribeMetadata::getService,
                                s -> getFullUrl(toCluster),
                                (o, n) -> n
                        )
                );

        // 组装
        return fails.stream()
                .map(fail -> toConsumeMessageByFail(fail, dbMap.get(fail.getMessageId())))
                .filter(Objects::nonNull)
                .filter(message -> {
                    boolean containUrl = serviceUrl.containsKey(message.getToService());
                    if (!containUrl) {
                        log.warn("找不到URL, cid:{}", message.getId());
                    }
                    return containUrl;
                }).collect(Collectors.groupingBy(m -> serviceUrl.get(m.getToService())));
    }

    @SneakyThrows
    private static String getFullUrl(String toCluster) {
        NamingService namingService = CourierContext.getBean(NamingServiceHolder.class).getNamingService();
        List<String> hosts = namingService.getAllInstances("courier-agent",
                        Collections.singletonList(toCluster)).stream()
                .map(instance -> "http://" + instance.getIp() + ":" + instance.getPort() + URLConstant.CLIENT_RECEIVES)
                .collect(Collectors.toList());
        return LoadBalancer.selectRandomHost(hosts);
    }

    @SneakyThrows
    private static String getFullUrl(String toCluster, String env) {
        NamingService namingService = CourierContext.getBean(NamingServiceHolder.class).getNamingService();
        List<String> hosts = namingService.getAllInstances("courier-agent",
                        Collections.singletonList(toCluster)).stream()
                .filter(instance -> env.equals(instance.getMetadata().get(ServiceRegistration.AGENT_ENV)))
                .map(instance -> "http://" + instance.getIp() + ":" + instance.getPort() + URLConstant.CLIENT_RECEIVES)
                .collect(Collectors.toList());
        return LoadBalancer.selectRandomHost(hosts);
    }

    /**
     * 将ConsumeFailMessage转换为ConsumeMessage
     */
    private static ConsumeMessage toConsumeMessageByFail(ConsumeFailMessage fail, DBMessage db) {

        if (db == null) {
            // 该消息可能是由 messageBus 发出的。
            log.warn("找不到DBMessage, cid:{}", fail.getId());
            return null;
        }

        if (db.getUsage() == Usage.BROADCAST) {
            log.warn("广播消息不重试, cid:{}", fail.getId());
            return null;
        }

        return new ConsumeMessage()
                .setId(fail.getId())
                .setMessageId(fail.getMessageId())
                .setTopic(fail.getTopic())
                .setType(db.getType())
                .setGroupId(fail.getGroupId())
                .setFromService(db.getFromService())
                .setToService(fail.getService())
                .setContent(db.getContent())
                .setCreatedAt(fail.getCreatedAt())
                .setPrimaryKey(fail.getPrimaryKey())
                .setUsage(fail.getUsage())
                .setRetries(fail.getRetries())
                .setNeedRepush(false)
                .setPollTime(null);
    }

    /**
     * 将ConsumeRecord转换为ConsumeMessage
     *
     * @return 例如：{@code http://10.0.0.1:11116/courier/messages/receive} -> [...]
     */
    public static Map<String, List<ConsumeMessage>> toConsumeMessagesByRecord(String toCluster, List<ConsumeRecord> records) {

        // 查询dbMessage
        Map<String, DBMessage> msgId2DbMsgMap = CourierContext.getBean(MessageMapper.class)
                .listByMessageIds(MessageQueryConditionConverter.toMessageQueryConditionByRecord(records))
                .stream()
                .collect(Collectors.toMap(DBMessage::getMessageId, Function.identity()));

        // 查询url
        Set<String> services = records.stream().map(ConsumeRecord::getToService).collect(Collectors.toSet());
        Map<String, String> type2urlMap = CourierContext.getBean(SubscribeBizService.class)
                .listByServices(toCluster, services)
                .stream()
                .collect(Collectors.toMap(SubscribeMetadata::getType, s -> getFullUrl(toCluster)));

        // 组装
        List<ConsumeMessage> consumeMessages = records.stream()
                .map(consumeRecord -> toConsumeMessageByRecord(consumeRecord, msgId2DbMsgMap.get(consumeRecord.getMessageId())))
                .filter(Objects::nonNull)
                .filter(message -> {
                    boolean containUrl = type2urlMap.containsKey(message.getType());
                    if (!containUrl) {
                        log.warn("找不到URL, cid:{}", message.getId());
                    }
                    return containUrl;
                })
                .collect(Collectors.toList());
        Map<String, List<ConsumeMessage>> map = new HashMap<>();
        for (ConsumeMessage consumeMessage : consumeMessages) {
            String fromCluster = consumeMessage.getFromCluster();
            String url;
            // 相同集群
            if (fromCluster.equals(toCluster)) {
                url = getFullUrl(toCluster, consumeMessage.getFromEnv());
            } else {// 不同集群
                url = type2urlMap.get(consumeMessage.getType());
            }
            map.computeIfAbsent(url, k -> new ArrayList<>()).add(consumeMessage);
        }
        return map;
    }

    /**
     * 将ConsumeRecord转换为ConsumeMessage
     */
    private static ConsumeMessage toConsumeMessageByRecord(ConsumeRecord consumeRecord, DBMessage db) {

        if (db == null) {
            // 该消息可能是由 messageBus 发出的。
            log.warn("找不到DBMessage, cid:{}, from service:{}", consumeRecord.getId(), consumeRecord.getFromService());
            return null;
        }

        if (db.getUsage() == Usage.BROADCAST) {
            log.warn("广播消息不重试, cid:{}", consumeRecord.getId());
            return null;
        }

        return new ConsumeMessage()
                .setId(consumeRecord.getId())
                .setMessageId(consumeRecord.getMessageId())
                .setTopic(consumeRecord.getTopic())
                .setType(db.getType())
                .setGroupId(consumeRecord.getGroupId())
                .setFromService(consumeRecord.getFromService())
                .setToService(consumeRecord.getToService())
                .setContent(db.getContent())
                .setCreatedAt(db.getCreatedAt())
                .setPrimaryKey(db.getPrimaryKey())
                .setUsage(db.getUsage())
                .setRetries(consumeRecord.getRetries())
                .setNeedRepush(false)
                .setPollTime(consumeRecord.getPollTime())
                .setFromCluster(db.getCluster())
                .setFromEnv(db.getEnv());
    }
}
