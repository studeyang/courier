package com.github.open.courier.client.metrics;

import com.github.open.courier.client.consumer.internal.ConsumeSupport;
import com.github.open.courier.client.consumer.internal.CourierClientProperties;
import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.support.CourierContext;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.open.courier.core.constant.MessageConstant.PRODUCER_EXECUTOR_BEAN;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/16
 */
@Data
public class ClientMetrics {

    private String time;
    private String service;

    private PoolMetrics producerPool;
    private PoolMetrics consumeAsyncPool;
    private PoolMetrics consumeSyncPool;
    private PoolMetrics consumeRetryPool;
    private Map<String, Object> consumeReporter;

    public ClientMetrics() {
        this.service = CourierContext.getProperty("spring.application.name");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.time = LocalDateTime.now().format(formatter);

        CourierClientProperties clientProperties = CourierContext.getBean(CourierClientProperties.class);
        ConsumeSupport consumeSupport = CourierContext.getBean(ConsumeSupport.class);

        initProducerPool(clientProperties.getProducer().getAsync().getQueueCapacity());
        initConsumeAsyncPool((ThreadPoolExecutor) consumeSupport.getAsyncExecutor(),
                clientProperties.getConsumer().getAsync().getQueueCapacity());
        initConsumeSyncPool((ThreadPoolExecutor) consumeSupport.getSyncExecutor(),
                clientProperties.getConsumer().getAsync().getQueueCapacity());
        initConsumeRetryPool((ThreadPoolExecutor) consumeSupport.getRetryExecutor(),
                clientProperties.getConsumer().getRetry().getQueueCapacity());


        this.consumeReporter = new ConsumeReportersMetrics(consumeSupport.getReporter(),
                clientProperties.getConsumer().getReport()).getMetrics();
    }

    private void initConsumeRetryPool(ThreadPoolExecutor pool, int queueMaxCapacity) {
        this.consumeRetryPool = new PoolMetrics(pool.getCorePoolSize(),
                pool.getActiveCount(),
                queueMaxCapacity,
                pool.getQueue().size());
    }

    private void initConsumeSyncPool(ThreadPoolExecutor pool, int queueMaxCapacity) {
        this.consumeSyncPool = new PoolMetrics(pool.getCorePoolSize(),
                pool.getActiveCount(),
                queueMaxCapacity,
                pool.getQueue().size());
    }

    private void initConsumeAsyncPool(ThreadPoolExecutor pool, int queueMaxCapacity) {
        this.consumeAsyncPool = new PoolMetrics(pool.getCorePoolSize(),
                pool.getActiveCount(),
                queueMaxCapacity,
                pool.getQueue().size());
    }

    private void initProducerPool(int queueMaxCapacity) {
        ThreadPoolExecutor pool = CourierContext.getBean(PRODUCER_EXECUTOR_BEAN, ThreadPoolExecutor.class);
        this.producerPool = new PoolMetrics(pool.getCorePoolSize(),
                pool.getActiveCount(),
                queueMaxCapacity,
                pool.getQueue().size());
    }

    @Override
    public String toString() {
        return MessageJsonConverter.toJson(this);
    }

    @Data
    @AllArgsConstructor
    static class PoolMetrics {
        /**
         * 核心线程数
         */
        private int corePoolSize;
        /**
         * 正在工作的线程数
         */
        private int activeCount;
        /**
         * 队列的最大容量
         */
        private int queueMaxCapacity;
        /**
         * 队列的当前任务数
         */
        private int queueCurrentTask;
    }

}
