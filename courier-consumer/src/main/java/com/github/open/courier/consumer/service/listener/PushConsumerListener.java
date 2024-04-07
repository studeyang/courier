package com.github.open.courier.consumer.service.listener;

import com.github.open.courier.consumer.service.support.NacosDiscoverySupport;
import com.github.open.courier.core.constant.URLConstant;
import com.github.open.courier.core.converter.ConsumeMessageConverter;
import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.consumer.service.support.RestTemplatePusher;
import com.github.open.courier.core.listener.ListenerState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * push模式的listener
 *
 * @author Administrator
 * @author <a href="https://github.com/studeyang">studeyang</a>
 */
@Slf4j
public class PushConsumerListener extends AbstractConsumerListener {

    private static final String HTTP = "http://";

    /**
     * 确保refresh时的可见性, 直接加volatile, sonar会报bug
     */
    @Getter
    private final AtomicReference<Map<String, String>> types;
    @Getter
    private final String service;
    @Getter
    private final String cluster;
    private final ConsumeRecordMapper consumeRecordMapper;
    private final RestTemplatePusher restTemplatePusher;
    private final NacosDiscoverySupport nacosDiscoverySupport;

    public PushConsumerListener(ConsumerListenerContainer.ConsumerGroup consumerGroup,
                                ConsumeRecordMapper consumeRecordMapper,
                                RestTemplatePusher restTemplatePusher,
                                NacosDiscoverySupport nacosDiscoverySupport) {
        super(consumerGroup);
        this.types = new AtomicReference<>(consumerGroup.getTypes());
        this.cluster = consumerGroup.getCluster();
        this.service = consumerGroup.getService();
        this.consumeRecordMapper = consumeRecordMapper;
        this.restTemplatePusher = restTemplatePusher;
        this.nacosDiscoverySupport = nacosDiscoverySupport;
    }

    /**
     * 处理消息
     */
    @Override
    public void handle(ConsumerRecords<String, String> records) {

        long start = System.currentTimeMillis();

        List<ConsumeMessage> messages = ConsumeMessageConverter.toConsumeMessages(records, types.get().keySet(), service, getGroupId());
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        if (log.isDebugEnabled()) {
            logOverView(messages);
        }

        try {
            consumeRecordMapper.insertBatch(messages);
        } catch (Exception e) {
            log.error("消息入库异常", e);
        }

        Map<Boolean, List<ConsumeMessage>> map = messages
                .stream()
                // 兼容 messagebus 发的消息
                .collect(Collectors.partitioningBy(
                        m -> StringUtils.equals(m.getFromCluster(), getCluster()))
                );

        // 相同集群的消息
        pushToSameCluster(getCluster(), map.get(true));

        // 不同集群的消息
        pushToDiffCluster(map.get(false));

        long cost = System.currentTimeMillis() - start;
        log.info("size={}, length={}, cost={}", messages.size(), messages.toString().length(), cost);
    }

    private void pushToDiffCluster(List<ConsumeMessage> messages) {
        // “不同集群” 只会是一个集群，因为每个 groupId 启动一个 consumer listener
        // 但要考虑有没有指定环境消费
        groupByUrl(messages).forEach(
                (url, msgs) -> {
                    if (log.isDebugEnabled()) {
                        String msgIds = msgs.stream()
                                .map(ConsumeMessage::getMessageId)
                                .collect(Collectors.joining(","));
                        // 来源集群可能会有多个
                        String fromCluster = messages.stream()
                                .map(ConsumeMessage::getFromCluster)
                                .distinct()
                                .collect(Collectors.joining(","));
                        log.debug("{} -> {}, 推给不同集群. url={}, messageIds={}", fromCluster, getCluster(), url, msgIds);
                    }
                    restTemplatePusher.pushByServiceOrUrl(url, service, msgs);
                }
        );
    }

    private void pushToSameCluster(String toCluster, List<ConsumeMessage> messages) {
        // 推送给同集群可以指定环境吗？
        // 不支持，避免出现乌龙：为什么我昆仑发出的消息被蓬莱消费了？结果是因为自己设置了蓬莱消息。
        messages.stream().collect(Collectors.groupingBy(ConsumeMessage::getFromEnv)).forEach(
                (env, msgs) -> {
                    String url = getFullUrlFromNacos(toCluster, env);
                    if (log.isDebugEnabled()) {
                        String msgIds = msgs.stream()
                                .map(ConsumeMessage::getMessageId)
                                .collect(Collectors.joining(","));
                        log.debug("{}.{} -> {}.{}, 推给相同集群. url={}, messageIds={}",
                                toCluster, env, toCluster, env, url, msgIds);
                    }
                    restTemplatePusher.pushByServiceOrUrl(url, getService(), msgs);
                }
        );
    }

    private String getFullUrlFromNacos(String toCluster, String env) {
        return HTTP + nacosDiscoverySupport.selectUrl(toCluster, env) + URLConstant.CLIENT_RECEIVES;
    }

    private String getFullUrlFromNacos(String toCluster) {
        return HTTP + nacosDiscoverySupport.selectUrl(toCluster) + URLConstant.CLIENT_RECEIVES;
    }

    private void logOverView(List<ConsumeMessage> messages) {
        Map<String, List<ConsumeMessage>> groupBy = new HashMap<>(4);
        for (ConsumeMessage message : messages) {
            String fromCluster = message.getFromCluster();
            groupBy.computeIfAbsent(fromCluster, k -> new ArrayList<>()).add(message);
        }
        StringBuilder overView = new StringBuilder();
        groupBy.forEach((from, msgs) -> {
            String messageIds = msgs.stream().map(ConsumeMessage::getMessageId).collect(Collectors.joining(","));
            overView.append(from).append("\n");
            overView.append("  |- ").append(messageIds).append("\n");
        });
        log.debug("overview for messages: \n{}", overView);
    }

    Map<String, List<ConsumeMessage>> groupByUrl(List<ConsumeMessage> messages) {
        Map<String, List<ConsumeMessage>> groupBy = new HashMap<>(4);
        String url = getFullUrlFromNacos(getCluster());
        for (ConsumeMessage message : messages) {
            // type 有没有指定消费
            String envTag = types.get().get(message.getType());
            if (StringUtils.isNotEmpty(envTag)) {
                String assignConsumeUrl = getFullUrlFromNacos(getCluster(), envTag);
                groupBy.computeIfAbsent(assignConsumeUrl, k -> new ArrayList<>()).add(message);
            } else {
                groupBy.computeIfAbsent(url, k -> new ArrayList<>()).add(message);
            }
        }
        return groupBy;
    }

    public void setTypes(Map<String, String> types) {
        this.types.set(types);
    }

    /**
     * 压测时, 得到的一个相对合理的配置, 限制fetch大小, 否则会OOM, 后续可改进
     */
    @Override
    public void additionalConfig(Map<String, Object> properties) {
        properties.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 50 * 1024);
    }

    @Override
    protected void pauseOrResumeIfNecessary(Consumer<String, String> consumer) {

        if (listenerSwitch.isPause()) {

            listenerSwitch.pauseConsume(consumer, "PUSH", service, getName());

        } else if (listenerSwitch.isResume()) {

            listenerSwitch.resumeConsume(consumer, "PUSH", service, getName());

        }
    }

    /* for metrics */

    public boolean getKafkaPaused() {
        return getListenerSwitch().getKafkaPaused();
    }

    public ListenerState getState() {
        return getListenerSwitch().getListenerState();
    }

}