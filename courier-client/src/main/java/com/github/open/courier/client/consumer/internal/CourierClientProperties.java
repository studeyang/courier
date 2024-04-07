package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.core.support.executor.PausableProperties;
import com.github.open.courier.core.support.executor.ThreadPoolProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

/**
 * courier客户端配置
 *
 * @author Courier
 */
@Slf4j
@Data
@ConfigurationProperties("courier")
public class CourierClientProperties {

    private ProducerProperties producer = new ProducerProperties();
    private ConsumerProperties consumer = new ConsumerProperties();

    /**
     * 打印courier的启动banner
     */
    private boolean banner = true;

    /**
     * 打印自定义配置的日志
     */
    private boolean logProperties = true;

    /**
     * topic前缀，groupid也会包含这部分
     */
    private String topicPrefix;

    @Data
    public static class ProducerProperties {

        /**
         * 消息批量发送的大小(与发送单条消息无关), 比如一次性发送10000条, 那么每500条发送一次, 发20次
         */
        private int partitionSize = 500;

        private AsyncProperties async = new AsyncProperties();

        public static class AsyncProperties extends ThreadPoolProperties {

            AsyncProperties() {
                super(3, 5, 60, 1000, CallerRunsPolicy.class, "kafka-async-producer-");
            }
        }
    }

    @Data
    public static class ConsumerProperties {

        private PullProperties pull = new PullProperties();
        private PushProperties push = new PushProperties();
        private AsyncProperties async = new AsyncProperties();
        private SyncProperties sync = new SyncProperties();
        private RetryProperties retry = new RetryProperties();
        private ReporterProperties report = new ReporterProperties();

        @Data
        public static class PullProperties extends PausableProperties {

            PullProperties() {
                super.setPause(new PauseProperties(true, 0.9, 0.5, 15));
            }

            /**
             * pull模式下的本地consumer的额外配置:
             *  注意: K-V都得是String, 不然max.poll.records: 123这样子的yml解析异常
             */
            private Map<String, String> config = new HashMap<>();
        }

        @Data
        public static class PushProperties extends PausableProperties {

            PushProperties() {
                super.setPause(new PauseProperties(false, 0.9, 0.5, 15));
            }
        }


        public static class AsyncProperties extends ThreadPoolProperties {

            AsyncProperties() {
                super(3, 5, 60, 1000, AbortPolicy.class, "kafka-async-consumer-");
            }
        }

        public static class SyncProperties extends ThreadPoolProperties {

            SyncProperties() {
                super(1, 1, 0, 1000, AbortPolicy.class, "kafka-sync-consumer-");
            }
        }

        @Data
        public static class RetryProperties extends ThreadPoolProperties {

            RetryProperties() {
                super(2, 5, 30, 1000, AbortPolicy.class, "kafka-retry-consumer-");
            }

            /**
             * 是否开启消费失败后重试机制
             */
            private boolean enable = false;
        }


        @Data
        public static class ReporterProperties {

            /**
             * 报告器类型
             */
            private Type type = Type.BUFFER;
            private AsyncProperties async = new AsyncProperties();
            private BufferProperties buffer = new BufferProperties();

            public enum Type {

                /**
                 * 同步
                 */
                SYNC,

                /**
                 * 异步
                 */
                ASYNC,

                /**
                 * 异步缓冲
                 */
                BUFFER
            }

            public static class AsyncProperties extends ThreadPoolProperties {

                AsyncProperties() {
                    super(3, 5, 60, 1000, CallerRunsPolicy.class, "kafka-async-reporter-");
                }
            }

            @Data
            public static class BufferProperties {

                TimedBufferProperties success = new TimedBufferProperties(500, 3, 1, 10000, "kafka-success-buffer-reporter-");
                TimedBufferProperties fail = new TimedBufferProperties(100, 5, 1, 2000, "kafka-fail-buffer-reporter-");

                @Data
                @AllArgsConstructor
                public static class TimedBufferProperties {

                    /**
                     * 每bufferSize个
                     */
                    private int bufferSize;

                    /**
                     * 每timeoutSeconds秒
                     */
                    private long timeoutSeconds;

                    /**
                     * 消费者数
                     */
                    private int coreConsumerSize;

                    /**
                     * 队列容量
                     */
                    private int queueCapacity;

                    /**
                     * 线程名前缀
                     */
                    private String threadNamePrefix;
                }
            }
        }
    }

    @PostConstruct
    public void init() {

        validate(consumer.pull.getPause().getPauseThresholdRate(), consumer.pull.getPause().getResumeThresholdRate());
        validate(consumer.push.getPause().getPauseThresholdRate(), consumer.push.getPause().getResumeThresholdRate());

        if (logProperties && log.isInfoEnabled()) {
            log.info("kafka配置: {}", MessageJsonConverter.toJson(this));
        }
    }

    private void validate(double pause, double resume) {

        if (pause <= 0 || pause > 1) {
            throw new IllegalArgumentException("pauseThresholdRate必须在(0, 1]之间, pause: " + pause);
        }
        if (resume < 0 || resume >= 1) {
            throw new IllegalArgumentException("resumeThresholdRate必须在[0, 1)之间, resume: " + resume);
        }
        if (pause <= resume) {
            throw new IllegalArgumentException("pauseThresholdRate必须大于resumeThresholdRate, pause: " + pause + ", resume: " + resume);
        }
    }
}
