package com.github.open.courier.client.metrics;

import com.github.open.courier.client.logging.CourierMetricsLog;
import com.github.open.courier.core.support.AutoStartupLifecycle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/studeyang">studeyang</a>
 * @since 2.0 2022/6/16
 */
@Slf4j
public class ClientMetricsRecorder extends AutoStartupLifecycle {

    private static final float PERCENT_10 = 0.1F;
    private static final long SECOND_10 = 10000;
    private static final long SECOND_15 = 15000;
    private static final long SECOND_30 = 30000;

    private volatile boolean isRunning;
    private ExecutorService executor;
    private ClientMetrics lastRecord;

    public ClientMetricsRecorder() {
        executor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new CustomizableThreadFactory("metrics-recorder-"));
    }

    @Override
    public void onStart() {
        isRunning = true;

        executor.execute(() -> {
            while (isRunning) {

                long start = System.currentTimeMillis();
                ClientMetrics clientMetrics = new ClientMetrics();
                long cost = System.currentTimeMillis() - start;

                CourierMetricsLog.info("{}", clientMetrics);

                lastRecord = clientMetrics;

                if (log.isDebugEnabled()) {
                    log.debug("record cost: {}", cost);
                }

                sleep(getNextRecordRate(clientMetrics));
            }
        });
    }

    private long getNextRecordRate(ClientMetrics clientMetrics) {
        int queueMaxCapacity = clientMetrics.getConsumeAsyncPool().getQueueMaxCapacity();
        int queueCurrentTask = clientMetrics.getConsumeAsyncPool().getQueueCurrentTask();
        int last = lastRecord.getConsumeAsyncPool().getQueueCurrentTask();

        if (queueCurrentTask == last) {
            // 相比于上次没变化，说明没什么任务在执行
            return SECOND_30;
        } else {
            // 相比于上次有变化，根据增长率计算
            float rate = Math.abs(queueCurrentTask - last) * 1.0F / queueMaxCapacity;
            if (rate > PERCENT_10) {
                return SECOND_10;
            }
        }
        return SECOND_15;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onStop() {
        isRunning = false;
        executor.shutdown();
    }
}
