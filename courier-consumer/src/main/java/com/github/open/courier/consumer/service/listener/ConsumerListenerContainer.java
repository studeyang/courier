package com.github.open.courier.consumer.service.listener;

import com.github.open.courier.consumer.service.support.NacosDiscoverySupport;
import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.consumer.service.support.RestTemplatePusher;
import com.github.open.courier.core.listener.AbstractListenerContainer;
import com.github.open.courier.core.listener.ListenerConfig;
import com.github.open.courier.core.transport.SubscribeMetadata;
import com.github.open.courier.repository.biz.SubscribeBizService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listener容器
 *
 * @author Courier
 */
@Slf4j
public class ConsumerListenerContainer extends AbstractListenerContainer {

    @Autowired
    private SubscribeBizService subscribeBizService;
    @Resource
    private ConsumeRecordMapper consumeRecordMapper;
    @Autowired
    private RestTemplatePusher restTemplatePusher;
    @Autowired
    private NacosDiscoverySupport nacosDiscoverySupport;

    @Override
    public List<AbstractConsumerListener> init() {

        List<AbstractConsumerListener> listeners = listConsumerGroup()
                .stream()
                .map(this::newPushConsumerListener)
                .collect(Collectors.toList());

        listeners.add(SubscribeConsumerListener.INSTANCE);

        return listeners;
    }

    private PushConsumerListener newPushConsumerListener(ConsumerGroup consumerGroup) {
        return new PushConsumerListener(consumerGroup, consumeRecordMapper, restTemplatePusher, nacosDiscoverySupport);
    }

    /**
     * 刷新listener容器, 以数据库中的最新newSubscribes为准
     */
    @Override
    public void refresh() {

        Map<String, ConsumerGroup> nameToConsumerGroup = listConsumerGroup()
                .stream()
                .collect(Collectors.toMap(ConsumerGroup::getName, Function.identity()));

        /*
         *   listenerMap(当前listeners)      nameToConsumerGroup(最新的listeners)
         *
         *   A - [t1, t2] - u1
         *   B - [t1, t2, t3] - u2           B - [t2, t3, t4] - u3
         *   C - [t1, t2] - u4               C - [t1, t2] - u4
         *                                   D - [t1, t2, t3] - u5
         *
         *   接下来要做的事:
         *   A - ×                    -> 要remove的旧listener
         *   B - [t2, t3, t4] - u3    -> 更新types的Listener(以最新的为准)
         *   C - [t1, t2] - u4        -> 更新types的Listener(以最新的为准, 哪怕types、url没有变动)
         *   D - [t1, t2, t3] - u5    -> 要add的新listener
         */

        // 新的listener中加上SubscribeListener
        Set<String> newNames = Sets.newHashSet(nameToConsumerGroup.keySet());
        newNames.add(SubscribeConsumerListener.INSTANCE.getName());

        Set<String> oldNames = getListenerMap().keySet();

        // 要remove的listener
        CollectionUtils.subtract(oldNames, newNames).forEach(this::stop);

        // 要add的listener
        CollectionUtils.subtract(newNames, oldNames).forEach(add ->
                start(newPushConsumerListener(nameToConsumerGroup.get(add))));

        // 要更新types、url的listener
        CollectionUtils.intersection(newNames, oldNames)
                .forEach(common -> {
                    AbstractConsumerListener listener = getListenerMap().get(common);
                    if (listener instanceof PushConsumerListener) {
                        PushConsumerListener consumerListener = (PushConsumerListener) listener;
                        ConsumerGroup consumerGroup = nameToConsumerGroup.get(common);

                        /*
                         * service不会变, 不存在 service-1 t-1 g-1, 直接变成 service-2 t-1 g-1的情况
                         * 这种情况, service-2直接订阅是不会订阅成功的, 只会分2步订阅:
                         *      1.service-1 取消订阅消息
                         *      2.service-2 订阅t-1 g-1
                         * 如果是上诉情况, 会调2次refresh, 第1步删除service-1, 第2次新增service-2, 不会到这里直接去修改service
                         */
                        consumerListener.setTypes(consumerGroup.getTypes());
                    }
                });
    }

    /**
     * 请求暂停（支持幂等）
     *
     * @param service 服务名
     */
    public void tryPause(String service) {

        log.info("kafka消费者请求暂停, service:{}", service);

        Set<String> names = getServiceToNamesMap().get(service);

        if (CollectionUtils.isEmpty(names)) {
            return;
        }

        for (String name : names) {
            Optional.ofNullable(getListenerMap().get(name))
                    .ifPresent(listener -> listener.getListenerSwitch().pause());
        }
    }

    /**
     * 请求恢复（支持幂等）
     *
     * @param service 服务名
     */
    public void tryResume(String service) {

        log.info("kafka消费者请求恢复, service:{}", service);

        Set<String> names = getServiceToNamesMap().get(service);

        if (CollectionUtils.isEmpty(names)) {
            return;
        }

        for (String name : names) {
            Optional.ofNullable(getListenerMap().get(name))
                    .ifPresent(listener -> listener.getListenerSwitch().resume());
        }
    }

    /**
     * 把SubscribeMetadata分组成ConsumerGroup, 对应到每个listener
     */
    List<ConsumerGroup> listConsumerGroup() {

        List<SubscribeMetadata> subscribes = subscribeBizService.listEnableService();

        List<ConsumerGroup> consumerGroups = Lists.newArrayList();

        subscribes.forEach(s -> {

            Optional<ConsumerGroup> consumerGroup = consumerGroups
                    .stream()
                    .filter(cg -> cg.getTopic().equals(s.getTopic()) && cg.getGroupId().equals(s.getGroupId()))
                    .findFirst();

            if (consumerGroup.isPresent()) {
                // 如果已存在, 就累加Type
                consumerGroup.get().getTypes().put(s.getType(), s.getEnv());
            } else {
                // 如果不存在, 新增一个ConsumerGroup, type只有当前的这个type
                consumerGroups.add(new ConsumerGroup(s));
            }
        });

        return consumerGroups;
    }

    private Map<String, Set<String>> getServiceToNamesMap() {

        Map<String, Set<String>> serviceToNamesMap = Maps.newHashMap();

        listConsumerGroup().forEach(consumerGroup -> {

            if (serviceToNamesMap.containsKey(consumerGroup.getService())) {

                serviceToNamesMap.get(consumerGroup.getService()).add(consumerGroup.getName());
            } else {

                Set<String> nameList = Sets.newHashSet(consumerGroup.getName());
                serviceToNamesMap.put(consumerGroup.getService(), nameList);
            }
        });

        return serviceToNamesMap;
    }

    /**
     * 消费者组
     */
    @Data
    static class ConsumerGroup implements ListenerConfig {

        /**
         * 订阅的主题
         */
        private final String topic;

        /**
         * 组ID
         */
        private final String groupId;

        private final String cluster;

        /**
         * 所在service
         */
        private final String service;

        /**
         * 所需的types
         */
        private final Map<String, String> types;

        public ConsumerGroup(SubscribeMetadata subscribe) {
            this.topic = subscribe.getTopic();
            this.groupId = subscribe.getGroupId();
            this.service = subscribe.getService();
            this.cluster = subscribe.getCluster();
            this.types = Maps.newHashMap();
            types.put(subscribe.getType(), subscribe.getEnv());
        }
    }

}
