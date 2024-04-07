package com.github.open.courier.commons.support;

import com.github.open.courier.core.support.executor.ThreadPoolProperties;
import com.google.common.collect.Sets;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import static com.github.open.courier.core.constant.MessageConstant.*;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/22
 */
@Data
@ConfigurationProperties("courier")
public class CourierServerProperties {

    private ProducerProperties producer = new ProducerProperties();

    private ConsumerProperties consumer = new ConsumerProperties();

    private DelayProperties delay = new DelayProperties();

    /**
     * common
     */
    private RestTemplateProperties restTemplate = new RestTemplateProperties();

    @Data
    public static class ConsumerProperties {

        /**
         * 本地环境会启动以下服务名的 Consumer Listener
         */
        private Set<String> defaultProfilesListenServices = Sets.newHashSet("courier-example");

    }

    @Data
    public static class RestTemplateProperties {

        private long readTimeoutMs = 5000L;

        private long connectTimeoutMs = 1000L;

        private Pool pool = new Pool();

        @Data
        public static class Pool {
            private int maxTotal = 200;
            private int maxPerRoute = 200;
            private int connectionRequestTimeout = 1000;
        }

    }

    @Data
    public static class ProducerProperties {

        /**
         * 消息体最大长度，默认 -1 表示不限制
         */
        private int messageMaxLength = 100000;

        /**
         * redis 异步入库的线程数
         */
        private int backupTaskCount = 10;

    }

    @Data
    public static class DelayProperties {

        /**
         * 打印自定义配置的日志
         */
        private boolean logProperties = true;

        /**
         * 主从模式配置信息
         */
        private SentinelProperties sentinel = new SentinelProperties();

        /**
         * 时间轮配置信息
         */
        private TimeWheelProperties timeWheel = new TimeWheelProperties();

        /**
         * 预读配置信息
         */
        private PreReadProperties preRead = new PreReadProperties();

        /**
         * 投递消息配置信息
         */
        private DeliverProperties deliver = new DeliverProperties();


        @Data
        public static class SentinelProperties {

            /**
             * Master节点竞争信号量
             */
            private String raceSign = DELAYMESSAGE_MASTERNODE_RACESIGN;

            /**
             * 信号量过期时间（单位ms）
             */
            private long signExpireTime = 15000;

            /**
             * Master节点续约时间（单位ms）
             */
            private long masterNodeRenewTime = 10000;

            /**
             * Slave节点心跳时间（单位ms）
             */
            private long slaveNodeHeartBeatTime = 8000;
        }


        @Data
        public static class TimeWheelProperties {

            /**
             * 时间轮开始索引
             */
            private int startPoint = 1;

            /**
             * 时间轮每圈时间间隔
             */
            private long perCycleTime = 60000;

            /**
             * 时间轮每个刻度时间间隔
             */
            private long perGridTime = 200;

            /**
             * 向前搜索的时间轮每圈时间间隔倍数：主要用户防止临界点的情况
             */
            private int forwardSearchThresholdRate = 10;

            /**
             * 向前搜索的时间轮每圈时间间隔倍数：主要用户防止临界点的情况
             */
            private int backwardSearchThresholdRate = 1;

            /**
             * 推入时间轮时管道里每个命令支持的消息量
             */
            private int pushPartitionSize = 500;

            /**
             * 从时间轮删除时管道里每个命令支持的消息量
             */
            private int deletePartitionSize = 500;

            /**
             * 时间轮刻度对应的Rediskey字符串
             */
            private String pointerSuffix = DELAYMESSAGE_TIMEWHEEL_POINTER;

            /**
             * 时间轮开始时间对应的rediskey
             */
            private String startTimeRedisKey = DELAYMESSAGE_TIMEWHEEL_STARTTIME;

            /**
             * 时间轮结束时间对应的rediskey
             */
            private String endTimeRedisKey = DELAYMESSAGE_TIMEWHEEL_ENDTIME;
        }


        @Data
        public static class PreReadProperties {

            /**
             * 每次消息处理量的大小
             */
            private int partitionSize = 200;

            /**
             * 标记预读状态线程池
             */
            private MarkReadedProperties markReaded = new MarkReadedProperties();


            public static class MarkReadedProperties extends ThreadPoolProperties {

                MarkReadedProperties() {
                    super(5, 10, 60, 1000, ThreadPoolExecutor.CallerRunsPolicy.class, "delay-mark-readed-");
                }
            }
        }


        @Data
        public static class DeliverProperties {

            /**
             * 每次消息处理量的大小
             */
            private int partitionSize = 200;

            /**
             * 分发投递线程池
             */
            private DispatchProperties dispatch = new DispatchProperties();

            /**
             * 异步投递线程池
             */
            private AsyncProperties async = new AsyncProperties();

            /**
             * 重试投递线程池
             */
            private RetryProperties retry = new RetryProperties();

            /**
             * 报告线程池
             */
            private ReporterProperties reporter = new ReporterProperties();


            public static class DispatchProperties extends ThreadPoolProperties {

                DispatchProperties() {
                    super(3, 5, 60, 500, ThreadPoolExecutor.CallerRunsPolicy.class, "delay-delive-dispatch-");
                }
            }

            public static class AsyncProperties extends ThreadPoolProperties {

                AsyncProperties() {
                    super(15, 20, 60, 1000, ThreadPoolExecutor.AbortPolicy.class, "delay-async-deliver-");
                }
            }

            public static class RetryProperties extends ThreadPoolProperties {

                RetryProperties() {
                    super(2, 5, 60, 500, ThreadPoolExecutor.AbortPolicy.class, "delay-retry-deliver-");
                }
            }

            public static class ReporterProperties extends ThreadPoolProperties {

                ReporterProperties() {
                    super(2, 5, 60, 500, ThreadPoolExecutor.AbortPolicy.class, "delay-delive-reporter-");
                }
            }

        }

    }

}
