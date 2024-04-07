package com.github.open.courier.core.support.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池暂停参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class PausableProperties {

    /**
     * 暂停属性配置
     */
    private PauseProperties pause = new PauseProperties();

    /**
     * 告警属性配置
     */
    private AlarmProperties alarm = new AlarmProperties();


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PauseProperties {

        /**
         * 是否开启告警机制
         */
        private boolean enable;

        /**
         * 暂停阈值率, 表示队列中的task数大于（队列容量 * 暂停阈值率）时, 会暂停consumer
         */
        private double pauseThresholdRate;

        /**
         * 恢复阈值率, 表示队列中的task数小于（队列容量 * 恢复阈值率） 时, 会恢复consumer
         */
        private double resumeThresholdRate;

        /**
         * 暂停与恢复请求之间的最小时间间隔
         */
        private int pauseAndResumeInterval;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlarmProperties {

        /**
         * 是否开启告警机制
         */
        private boolean enable = true;

        /**
         * 告警器类型
         */
        private Type type = Type.ASYNC;


        private AsyncProperties async = new AsyncProperties();


        public enum Type {

            /**
             * 同步
             */
            SYNC,

            /**
             * 异步
             */
            ASYNC
        }

        public static class AsyncProperties extends ThreadPoolProperties {

            AsyncProperties() {

                super(1, 1, 0, 1000, ThreadPoolExecutor.AbortPolicy.class, "kafka-async-alarm-");
            }
        }
    }

}
