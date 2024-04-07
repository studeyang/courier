package com.github.open.courier.core.support.executor;

import com.github.open.courier.core.support.CourierContext;
import com.github.open.courier.core.utils.DateUtils;
import com.github.open.courier.core.utils.IpUtils;
import com.github.open.courier.core.listener.ListenerState;
import com.github.open.courier.core.transport.ThreadPoolAlarmMetadata;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.time.LocalDateTime;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.open.courier.core.listener.ListenerState.PAUSE;
import static com.github.open.courier.core.listener.ListenerState.RESUME;

/**
 * 可暂停的线程池
 *
 * @author Courier
 */
@Getter
@Slf4j
public class PausableThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * 是否开启暂停机制
     */
    private final boolean pauseEnable;

    /**
     * 是否开启告警机制
     */
    private final boolean alarmEnable;

    /**
     * 主机IP地址
     */
    private final String nodeIp;

    /**
     * 队列容量
     */
    private final int queueCapacity;

    /**
     * 暂停阈值
     */
    private final int pauseThreshold;

    /**
     * 恢复阈值
     */
    private final int resumeThreshold;

    /**
     * 暂停与恢复请求之间的最小时间间隔
     */
    private final int pauseAndResumeInterval;

    /**
     * 执行告警或恢复通知的处理器
     */
    private final AlarmCallback alarmCallback;

    /**
     * 执行暂停与恢复的处理器
     */
    private final PauseCallback pauseCallback;

    private ScheduledExecutorService scheduledExecutorService;

    private Cache<String, Object> pauseCache;

    public PausableThreadPoolExecutor(ThreadPoolProperties threadPoolProperties,
                                      PausableProperties pausableProperties,
                                      PauseCallback pauseCallback,
                                      AlarmCallback alarmCallback) {

        super(threadPoolProperties.getCorePoolSize(),
                threadPoolProperties.getMaxPoolSize(),
                threadPoolProperties.getKeepAliveSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(threadPoolProperties.getQueueCapacity()),
                new CustomizableThreadFactory(threadPoolProperties.getThreadNamePrefix()),
                threadPoolProperties.instanceHandler()
        );

        this.pauseCallback = pauseCallback;
        this.alarmCallback = alarmCallback;
        this.nodeIp = IpUtils.getHostAddress();

        this.queueCapacity = threadPoolProperties.getQueueCapacity();
        this.pauseEnable = pausableProperties.getPause().isEnable();
        this.alarmEnable = pausableProperties.getAlarm().isEnable();

        this.pauseThreshold = (int) (pausableProperties.getPause().getPauseThresholdRate() * queueCapacity);
        this.resumeThreshold = (int) (pausableProperties.getPause().getResumeThresholdRate() * queueCapacity);
        this.pauseAndResumeInterval = pausableProperties.getPause().getPauseAndResumeInterval();

        if (needPauseOrResume()) {
            this.pauseCache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(pauseAndResumeInterval, TimeUnit.SECONDS).build();
            this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new CustomizableThreadFactory("resumecheck-sentinel-"));
            this.scheduledExecutorService.scheduleAtFixedRate(new ResumeCheckSentinel(), 0, pauseAndResumeInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * 在提交task时, 判断是否暂停
     */
    @Override
    public void execute(Runnable r) {
        this.doCheckPause();
        super.execute(r);
    }

    /**
     * 在task从队列中移除, 马上要执行前, 判断是否恢复
     * 暂时不依赖这个方法执行恢复的逻辑，调试的过程中发现，只有一个任务触发暂停时，将没有办法执行恢复操作
     * 所以做了一个定时巡检的哨兵
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {

    }

    /**
     * task执行完后, debug下, 可有可无
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (log.isDebugEnabled()) {
            log.debug("kafka消费线程池队列size:{}", getQueue().size());
        }
    }

    /**
     * 计算一个listener一次poll的最大条数
     */
    public int calculateMaxPollRecords(int listenerCount) {
        int i = (queueCapacity - pauseThreshold + (getMaximumPoolSize() - getCorePoolSize())) / listenerCount;
        // 最小10(太少了效率不行), 最大500(kafka默认值)
        return Math.min(Math.max(i, 10), 500);
    }

    /**
     * 检查并决定是否暂停
     */
    public void doCheckPause() {
        if (!needPauseOrResume()) {
            return;
        }

        int queueSize = getCurrentThreadPoolQueueSize();
        // 条件1：上一次发送的是恢复请求 条件2：流量不正常
        if (StateMonitor.isLastResume() && queueSize >= pauseThreshold) {
            String date = DateUtils.formatDate(LocalDateTime.now());
            pauseCache.put(CourierContext.getService(), date);

            if (alarmEnable) {
                alarmCallback.alarm(new ThreadPoolAlarmMetadata()
                        .setNodeIp(nodeIp)
                        .setQueueCapacity(queueCapacity)
                        .setPauseThreshold(pauseThreshold)
                        .setQueueSize(queueSize));
            }

            if (pauseEnable) {
                pauseCallback.pause();
            }

            StateMonitor.setPause();
        }
    }

    /**
     * 检查并决定是否恢复
     */
    public void doCheckResume() {

        if (!needPauseOrResume()) {
            return;
        }

        int queueSize = getCurrentThreadPoolQueueSize();

        // 条件1：流量正常  条件2：暂停15秒以上  条件3：上一次发送的是暂停请求
        if (StateMonitor.isLastPause() && queueSize <= resumeThreshold
                && !pauseCache.asMap().containsKey(CourierContext.getService())) {

            if (alarmEnable) {
                alarmCallback.recovery(new ThreadPoolAlarmMetadata()
                        .setNodeIp(nodeIp)
                        .setQueueCapacity(queueCapacity)
                        .setResumeThreshold(resumeThreshold)
                        .setQueueSize(queueSize));
            }

            if (pauseEnable) {
                pauseCallback.resume();
            }

            StateMonitor.setResume();
        }
    }

    /**
     * 获取线程池队列大小
     */
    public int getCurrentThreadPoolQueueSize() {
        return getQueue().size();
    }

    /**
     * 判断是否需要执行暂停或恢复逻辑
     */
    public boolean needPauseOrResume() {
        return alarmEnable || pauseEnable;
    }

    /**
     * 获取监视器的状态
     */
    public ListenerState getListenerState() {
        return StateMonitor.getListenerState();
    }

    /**
     * 恢复巡检哨兵
     */
    class ResumeCheckSentinel implements Runnable {

        @Override
        public void run() {

            try {
                doCheckResume();
            } catch (Exception e) {
                log.error("线程池自动恢复检查哨兵执行失败", e);
            }
        }
    }

    /**
     * 状态监视器
     */
    private static class StateMonitor {

        private static final AtomicReference<ListenerState> lastSend = new AtomicReference<>(RESUME);

        /**
         * 上次消费状态是暂停状态？
         */
        public static boolean isLastPause() {
            return lastSend.get() == PAUSE;
        }

        /**
         * 上次消费状态是恢复状态？
         */
        public static boolean isLastResume() {
            return lastSend.get() == RESUME;
        }

        /**
         * 设置消费状态为暂停状态
         */
        public static void setPause() {
            lastSend.compareAndSet(RESUME, PAUSE);
        }

        /**
         * 设置消费状态为恢复状态
         */
        public static void setResume() {
            lastSend.compareAndSet(PAUSE, RESUME);
        }

        /**
         * 获取消费状态监视器状态
         */
        public static ListenerState getListenerState() {
            return lastSend.get();
        }
    }

}
