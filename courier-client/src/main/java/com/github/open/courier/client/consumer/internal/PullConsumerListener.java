package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.client.feign.ConsumerClient;
import com.github.open.courier.core.converter.ConsumeMessageConverter;
import com.github.open.courier.core.listener.AbstractConsumerListener;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.core.listener.ListenerConfig;
import com.github.open.courier.core.message.Usage;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.transport.TopicGroup;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * pull模式的listener
 *
 * @author Administrator
 */
@Slf4j
public class PullConsumerListener extends AbstractConsumerListener {

    private final Set<String> types;
    private final String service;
    private final ConsumerClient consumerClient;
    private final ConsumeSupport consumeSupport;
    @Getter
    private final Map<String, String> config;

    public PullConsumerListener(TopicGroup topicGroup, Map<String, String> config) {
        super(new WrappedListenerConfig(topicGroup));
        this.types = topicGroup.getTypes();
        this.config = config;
        this.service = CourierContext.getService();
        this.consumerClient = CourierContext.getBean(ConsumerClient.class);
        this.consumeSupport = CourierContext.getBean(ConsumeSupport.class);
    }

    /**
     * 包装 topic & groupId
     */
    @Value
    static class WrappedListenerConfig implements ListenerConfig {

        String topic;
        String groupId;

        WrappedListenerConfig(ListenerConfig unWrapped) {
            this.topic = Wrapper.wrapTopic(unWrapped.getTopic());
            this.groupId = Wrapper.wrapGroupId(unWrapped.getGroupId());
        }
    }

    @Override
    public void handle(ConsumerRecords<String, String> records) {

        List<ConsumeMessage> messages = ConsumeMessageConverter.toConsumeMessages(records, types, service, getGroupId());

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        // 重试
        List<ConsumeMessage> retryMessages = null;
        // 先记录, 再消费
        try {
            consumerClient.record(messages);
        } catch (Exception e) {
            retryMessages = messages;
            log.error("kafka消息入库失败, messages:{}", messages, e);
        }

        // 根据消息的类型进行分发(事件/广播)
        Map<Boolean, List<ConsumeMessage>> map = messages.stream().collect(Collectors.partitioningBy(m -> m.getUsage() == Usage.EVENT));

        // 消费事件
        consumeSupport.consume(map.get(true));

        // 消费广播
        List<ConsumeMessage> broadcasts = map.get(false);
        if (CollectionUtils.isNotEmpty(broadcasts)) {
            try {
                consumerClient.consumeBroadcast(broadcasts);
            } catch (Exception e) {
                log.error("kafka消费广播消息失败, messages:{}", broadcasts, e);
            }
        }

        if (CollectionUtils.isNotEmpty(retryMessages)) {
            consumerClient.record(messages);
        }
    }

    /**
     * 额外的自定义配置
     */
    @Override
    public void additionalConfig(Map<String, Object> properties) {
        properties.putAll(config);
    }


    @Override
    protected void pauseOrResumeIfNecessary(Consumer<String, String> consumer) {

        if (PullSwitch.isPaused()) {
            listenerSwitch.pauseConsume(consumer, "PULL", service, getName());
        } else {
            listenerSwitch.resumeConsume(consumer, "PULL", service, getName());
        }
    }


    /**
     * 【PULL】模式下的暂停开关
     */
    static class PullSwitch {


        private static AtomicBoolean holder = new AtomicBoolean();

        /**
         * 执行PULL暂停
         */
        public static void pause() {
            holder.compareAndSet(false, true);
        }

        /**
         * 执行PULL恢复
         */
        public static void resume() {
            holder.compareAndSet(true, false);
        }

        /**
         * 是否是暂停状态
         */
        public static boolean isPaused() {
            return holder.get();
        }
    }

    /* for metrics */

    public int getTypes() {
        return types.size();
    }

    public boolean getKafkaPaused() {
        return getListenerSwitch().getKafkaPaused();
    }

}
