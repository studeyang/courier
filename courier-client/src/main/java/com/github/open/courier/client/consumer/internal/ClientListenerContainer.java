package com.github.open.courier.client.consumer.internal;

import static com.github.open.courier.core.constant.MessageConstant.LISTENER_CONTAINER_PHASE;
import static com.github.open.courier.core.support.CourierContext.getBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.core.support.CourierContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import com.github.open.courier.core.listener.AbstractListenerContainer;
import com.github.open.courier.core.support.executor.PausableThreadPoolExecutor;
import com.github.open.courier.core.transport.TopicGroup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * client端的listener容器
 */
@Slf4j
@RequiredArgsConstructor
public class ClientListenerContainer extends AbstractListenerContainer {

    private final MessageHandlerContainer handlerContainer;
    private final Map<String, String> config;

    @Override
    public List<AbstractConsumerListener> init() {

        // 开发环境
        boolean startWithPull = CourierContext.isDevEnvironment();

        if (!startWithPull) {
            return Collections.emptyList();
        }

        Set<TopicGroup> topicGroups = handlerContainer.getSubscribeRequest().getTopicGroups();

        if (CollectionUtils.isEmpty(topicGroups)) {
            return Collections.emptyList();
        }

        if (CourierContext.isDevEnvironment()) {
            log.info("开发环境, 使用pull模式启动kafka");
        }

        // 计算单个listener的最大poll条数, 保证在极端条件下, 队列能容纳暂停后的一次poll的消息量
        config.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                           Integer.toString(CourierContext.getBean(PausableThreadPoolExecutor.class).calculateMaxPollRecords(topicGroups.size())));

        return topicGroups
                .stream()
                .map(tg -> new PullConsumerListener(tg, config))
                .collect(Collectors.toList());
    }

    @Override
    public void refresh() {
        log.warn("client端container刷新, 啥都没发生~");
    }

    @Override
    public int getPhase() {
        return LISTENER_CONTAINER_PHASE;
    }
}
