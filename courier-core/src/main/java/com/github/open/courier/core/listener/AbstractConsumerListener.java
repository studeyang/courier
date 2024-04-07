package com.github.open.courier.core.listener;

import com.github.open.courier.core.support.Brokers;
import com.github.open.courier.core.support.Retryable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.WaitStrategies;
import com.github.rholder.retry.WaitStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消费者listener
 *
 * @author Courier
 */
@Slf4j
@Getter
public abstract class AbstractConsumerListener implements Runnable {

    private static AtomicInteger count = new AtomicInteger();
    @JsonIgnore
    private final String topic;
    @JsonIgnore
    private final String groupId;
    private final String name;
    @JsonIgnore
    protected final ListenerSwitch listenerSwitch;

    protected AbstractConsumerListener(ListenerConfig config) {
        this.topic = config.getTopic();
        this.groupId = config.getGroupId();
        this.name = config.getName();
        this.listenerSwitch = new ListenerSwitch();
    }

    /**
     * 如果消费过程中遇到异常, 不断重试
     */
    @Override
    @SneakyThrows
    public void run() {
        Thread.currentThread().setName("kafka-listener-" + count.incrementAndGet() + " | " + name);
        RetryerHolder.retryer.call(Executors.callable(this::poll));
    }

    /**
     * 轮询拉取消息并消费
     */
    private void poll() {

        Consumer<String, String> consumer = new KafkaConsumer<>(getProperties());

        try { // NOSONAR, finally在close前, 需要同步commit一次, 所以不能直接用try-with-resources

            consumer.subscribe(Collections.singleton(topic), new RebalanceListener(this, consumer));

            log.info("kafka消费者开始消费");

            while (!listenerSwitch.isStop()) {

                pauseOrResumeIfNecessary(consumer);

                ConsumerRecords<String, String> records = consumer.poll(1000);

                if (records == null || records.isEmpty()) {
                    continue;
                }

                log.debug("kafka拉到消息, size:{}", records.count());

                handle(records);

                consumer.commitAsync((o, e) -> {
                    if (e == null) {
                        log.debug("kafka消费者提交offset成功, offset:{}", o);
                    } else {
                        log.warn("kafka消费者提交offset失败", e);
                    }
                });
            }

            log.info("kafka消费者停止消费");

        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    /**
     * 暂停或恢复 poll 消息
     *
     * @param consumer kafka 消费者
     */
    protected abstract void pauseOrResumeIfNecessary(Consumer<String, String> consumer);

    /**
     * RebalanceListener
     */
    @Slf4j
    @RequiredArgsConstructor
    static class RebalanceListener implements ConsumerRebalanceListener {

        final AbstractConsumerListener listener;
        final Consumer<String, String> consumer;

        /**
         * 在Rebalance前, 同步提交即将失去的TopicPartition
         */
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            log.info("kafka 再平衡-开始(Revoked), partitions:{}", partitions);
            consumer.commitSync();
        }

        /**
         * 在Rebalance后, 继续暂停原本已经暂停的consumer
         */
        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            log.info("kafka 再平衡-结束(Assigned), partitions:{}", partitions);
            if (listener.listenerSwitch.isPause()) {
                listener.listenerSwitch.setKafkaPaused(true);
                consumer.pause(partitions);
                log.info("rebalance后, kafka消费者继续暂停, paused:{}", partitions);
            }
        }
    }

    /**
     * 提供最基本的配置, 子类可以通过additionalProperties()添加自定义配置
     */
    @JsonIgnore
    public final Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Brokers.get());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        additionalConfig(properties);
        return properties;
    }

    /**
     * 子类可以Override该方法来添加自定义配置
     */
    public void additionalConfig(Map<String, Object> properties) {
    }

    /**
     * 停止consumer
     */
    public final void stop() {
        log.info("kafka消费者正在关闭, topic:{}, groupId:{}", topic, groupId);
        listenerSwitch.stop();
    }

    /**
     * 处理消息
     *
     * @param records 消息
     */
    public abstract void handle(ConsumerRecords<String, String> records);

    /**
     * 重试器, 无状态, 所以线程安全, 所以弄成单例
     */
    enum RetryerHolder implements Retryable<Object> {

        /**
         * 单例实例
         */
        INSTANCE;

        /**
         * 重试器
         */
        static Retryer<Object> retryer = INSTANCE.build();

        /**
         * 重试间隔, 1s 2s 4s 8s 16s 30s 30s 30s 30s ...
         */
        @Override
        public WaitStrategy getWaitStrategy() {
            return WaitStrategies.exponentialWait(500, 30, TimeUnit.SECONDS);
        }

        /**
         * 遇到异常重试时, 打印异常信息
         */
        @Override
        public <V> void onRetry(Attempt<V> attempt) {
            if (attempt.hasException()) {
                log.error("kafka消费者异常", attempt.getExceptionCause());
            }
        }
    }

}
