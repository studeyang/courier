package com.github.open.courier.client.metrics;

import com.github.open.courier.client.consumer.internal.ConsumeReporters;
import com.github.open.courier.client.consumer.internal.CourierClientProperties;
import com.github.open.courier.core.transport.MessageConsumeTime;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/16
 */
public class ConsumeReportersMetrics extends ConsumeReporters {

    @Getter
    private Map<String, Object> metrics = new LinkedHashMap<>();

    public ConsumeReportersMetrics(ConsumeReporter consumeReporter, CourierClientProperties.ConsumerProperties.ReporterProperties reporterProperties) {
        if (consumeReporter instanceof ConsumeReporters.BufferReporter) {
            // 缓冲报告，监控缓冲队列
            BufferReporter bufferReporter = (BufferReporter) consumeReporter;
            CourierClientProperties.ConsumerProperties.ReporterProperties.BufferProperties bufferProperties = reporterProperties.getBuffer();

            BlockingQueue<MessageConsumeTime> queue = bufferReporter.getSuccessBuffer().getQueue();
            ThreadPoolExecutor pool = (ThreadPoolExecutor) bufferReporter.getSuccessBuffer().getConsumers();
            metrics.put("corePoolSize", bufferProperties.getSuccess().getCoreConsumerSize());
            metrics.put("activeCount", pool == null ? 0 : pool.getActiveCount());
            metrics.put("queueMaxCapacity", bufferProperties.getSuccess().getQueueCapacity());
            metrics.put("queueCurrentTask", queue.size());

        } else if (consumeReporter instanceof ConsumeReporters.AsyncReporter) {
            // 异步报告，监控线程池
            AsyncReporter asyncReporter = (AsyncReporter) consumeReporter;
            CourierClientProperties.ConsumerProperties.ReporterProperties.AsyncProperties asyncProperties = reporterProperties.getAsync();

            ThreadPoolExecutor pool = (ThreadPoolExecutor) asyncReporter.getExecutor();
            metrics.put("corePoolSize", asyncProperties.getCorePoolSize());
            metrics.put("activeCount", pool.getActiveCount());
            metrics.put("queueMaxCapacity", asyncProperties.getQueueCapacity());
            metrics.put("queueCurrentTask", pool.getQueue().size());
        } else {
            // 同步报告，没什么可监控的
        }
    }

}
