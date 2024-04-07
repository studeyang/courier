package com.github.open.courier.autoconfigure;

import com.github.open.casslog.core.logging.AbstractLogExtend;
import com.github.open.casslog.core.logging.LogExtendInitializer;
import com.github.open.courier.client.consumer.ConsumeService;
import com.github.open.courier.client.consumer.internal.*;
import com.github.open.courier.client.consumer.internal.ConsumeReporters.ConsumeReporter;
import com.github.open.courier.client.feign.ConsumerClient;
import com.github.open.courier.client.feign.DelayClient;
import com.github.open.courier.client.feign.ManagementClient;
import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.client.logging.CourierLogExtend;
import com.github.open.courier.client.metrics.ClientMetricsRecorder;
import com.github.open.courier.client.metrics.ClientMetricsService;
import com.github.open.courier.client.producer.DefaultMessagePublisher;
import com.github.open.courier.client.producer.DelayMessagePublisher;
import com.github.open.courier.client.producer.MessagePublisher;
import com.github.open.courier.client.producer.sender.*;
import com.github.open.courier.core.support.Brokers;
import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.support.CourierVersion;
import com.github.open.courier.core.support.Wrapper;
import com.github.open.courier.core.support.executor.*;
import com.github.open.courier.eventing.BroadcastPublisher;
import com.github.open.courier.eventing.EventPublisher;
import io.github.open.toolkit.config.annotation.PrepareConfigurations;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static com.github.open.courier.core.constant.MessageConstant.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * @author Courier
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(CourierClientProperties.class)
@EnableFeignClients(clients = {ProducerClient.class, DelayClient.class, ConsumerClient.class, ManagementClient.class})
@org.springframework.cloud.openfeign.EnableFeignClients(clients = {
        ProducerClient.class, DelayClient.class, ConsumerClient.class, ManagementClient.class})
@PrepareConfigurations({"__common_feign_client_", "__common_message_client2_"})
public class CourierClientAutoConfiguration {

    @Bean(COURIER_CONTEXT_BEAN)
    public CourierContext courierContext() {
        return new CourierContext();
    }

    @Bean
    public CourierStarterBanner courierStarterBanner() {
        return new CourierStarterBanner();
    }

    @Bean
    public CourierVersion courierVersion() {
        String version = Optional.ofNullable(getClass().getPackage())
                .map(Package::getImplementationVersion)
                .orElse(EMPTY);
        return new CourierVersion(version);
    }

    /* 日志扩展配置 */

    @Bean
    public CourierLogExtend courierLogExtend() {
        return new CourierLogExtend();
    }

    @Bean
    @ConditionalOnMissingBean(LogExtendInitializer.class)
    public LogExtendInitializer logExtendInitializer(List<AbstractLogExtend> logExtends) {
        return new LogExtendInitializer(logExtends);
    }

    /* 客户端监控看板 */

    @Bean
    public ClientMetricsRecorder clientMetricsRecorder() {
        return new ClientMetricsRecorder();
    }

    @Configuration
    static class ProducerAutoConfiguration {

        /**
         * 生产者发送线程池
         */
        @Bean(PRODUCER_EXECUTOR_BEAN)
        public ExecutorService producerExecutor(CourierClientProperties properties) {
            return properties.getProducer().getAsync().create();
        }

        @Bean
        public Wrapper wrapper(CourierClientProperties properties) {
            // customizeTopicPrefix() -> init()
            Wrapper wrapper = new Wrapper();
            if (StringUtils.isNotEmpty(properties.getTopicPrefix())) {
                wrapper.customizeTopicPrefix(properties.getTopicPrefix());
            }
            return wrapper;
        }

        @Bean
        public Reportable simpleReporter() {
            return new SimpleReporter();
        }

        @Bean
        @Qualifier("defaultMessagePublisher")
        public MessagePublisher defaultMessagePublisher(ProducerClient producerClient,
                                                        Reportable simpleReporter,
                                                        ManagementClient managementClient,
                                                        CourierClientProperties properties) {
            ProducerSender producerSender = new ProducerSender(producerClient, properties.getProducer().getPartitionSize());
            RetryableSender retryableSender = new RetryableSender(producerSender);
            ReportableSender reportableSender = new ReportableSender(retryableSender, simpleReporter, managementClient,
                    properties.getProducer().getPartitionSize());
            return new DefaultMessagePublisher(reportableSender);
        }

        @Bean
        @Qualifier("delayMessagePublisher")
        public MessagePublisher delayMessagePublisher(DelayClient delayClient,
                                                      Reportable simpleReporter,
                                                      ManagementClient managementClient,
                                                      CourierClientProperties properties) {
            DelaySender delaySender = new DelaySender(delayClient, properties.getProducer().getPartitionSize());
            RetryableSender retryableSender = new RetryableSender(delaySender);
            ReportableSender reportableSender = new ReportableSender(retryableSender, simpleReporter, managementClient,
                    properties.getProducer().getPartitionSize());
            return new DelayMessagePublisher(reportableSender);
        }

        @Autowired
        public void setDefaultPublisher(MessagePublisher defaultMessagePublisher) {
            EventPublisher.setPublisher(defaultMessagePublisher);
            BroadcastPublisher.setPublisher(defaultMessagePublisher);
        }

        @Autowired
        public void setDelayPublisher(MessagePublisher delayMessagePublisher) {
            EventPublisher.setDelayPublisher(delayMessagePublisher);
            BroadcastPublisher.setDelayPublisher(delayMessagePublisher);
        }

        @Bean
        public TransactionMessageConfiguration transactionMessageConfiguration() {
            return new TransactionMessageConfiguration();
        }
    }

    @Configuration
    static class ConsumerAutoConfiguration {

        /**
         * 消费者异步消费线程池
         */
        @Bean(CONSUMER_ASYNC_EXECUTOR_BEAN)
        public ExecutorService consumerAsyncExecutor(CourierClientProperties properties,
                                                     ProducerClient producerClient,
                                                     ConsumerClient consumerClient) {

            ThreadPoolProperties threadPoolProperties = properties.getConsumer().getAsync();

            PausableProperties pausableProperties = properties.getConsumer().getPush();

            PauseCallback pauseCallback = PauseCallbackFactory.newInstance(producerClient);
            AlarmCallback alarmCallback = AlarmCallbackFactory.newInstance(pausableProperties.getAlarm(), consumerClient);

            return new PausableThreadPoolExecutor(threadPoolProperties, pausableProperties, pauseCallback, alarmCallback);
        }

        /**
         * 消费者同步消费线程池
         */
        @Bean(CONSUMER_SYNC_EXECUTOR_BEAN)
        public ExecutorService consumerSyncExecutor(CourierClientProperties properties) {
            return properties.getConsumer().getSync().create();
        }


        /**
         * 消费者重试线程池
         */
        @Bean(CONSUMER_RETRY_EXECUTOR_BEAN)
        public ExecutorService consumerRetryExecutor(CourierClientProperties properties) {
            return properties.getConsumer().getRetry().create();
        }

        @Bean
        public ConsumeReporter consumeReporter(CourierClientProperties properties, ManagementClient client) {
            return ConsumeReporters.newInstance(properties.getConsumer().getReport(), client);
        }

        @Bean
        public Brokers brokers() {
            return new Brokers();
        }

        @Bean
        @DependsOn({COURIER_CONTEXT_BEAN, "courierVersion"})
        public MessageHandlerContainer messageHandlerContainer() {
            return new MessageHandlerContainer();
        }

        @Bean
        public SubscribeInitializer subscribeInitializer(MessageHandlerContainer handlerContainer, ProducerClient producerClient) {
            return new SubscribeInitializer(handlerContainer, producerClient);
        }

        @Bean
        public ClientListenerContainer clientListenerContainer(MessageHandlerContainer handlerContainer, CourierClientProperties properties) {
            return new ClientListenerContainer(handlerContainer, properties.getConsumer().getPull().getConfig());
        }

        @Bean
        public ConsumeSupport consumeSupport(@Qualifier(CONSUMER_ASYNC_EXECUTOR_BEAN) ExecutorService consumerAsyncExecutor,
                                             @Qualifier(CONSUMER_SYNC_EXECUTOR_BEAN) ExecutorService consumerSyncExecutor,
                                             @Qualifier(CONSUMER_RETRY_EXECUTOR_BEAN) ExecutorService consumerRetryExecutor,
                                             ConsumeReporter consumeReporter,
                                             MessageHandlerContainer handlerContainer,
                                             CourierClientProperties properties) {
            return new ConsumeSupport(consumerAsyncExecutor,
                    consumerSyncExecutor,
                    consumerRetryExecutor,
                    consumeReporter,
                    handlerContainer,
                    properties.getConsumer().getRetry().isEnable());
        }

        @Bean
        public ConsumeService consumeService(ConsumeSupport consumeSupport) {
            return new ConsumeService(consumeSupport);
        }

        @Bean
        public ClientMetricsService clientMetricsService(ClientListenerContainer clientListenerContainer) {
            return new ClientMetricsService(clientListenerContainer);
        }

    }

}
