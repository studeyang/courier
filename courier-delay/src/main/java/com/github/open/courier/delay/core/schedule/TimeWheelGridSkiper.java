package com.github.open.courier.delay.core.schedule;

import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.delay.core.support.delive.DeliverSupport;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 时间轮刻度拨动器
 *
 * @author wangyonglin
 */
@Slf4j
public class TimeWheelGridSkiper {

    /**
     * 延迟消息预读器和时间轮刻度拨动器的通信协调者
     */
    private final PreReadAndSkipCoordinator coordinator;
    private final RedisTimeWheel redisTimeWheel;
    private final DeliverSupport deliverSupport;


    private volatile boolean skiperStop;
    private Thread skiper;


    public TimeWheelGridSkiper(PreReadAndSkipCoordinator coordinator,
                               ScheduleManager scheduleManager,
                               DeliverSupport deliverSupport) {

        this.coordinator = coordinator;
        this.redisTimeWheel = scheduleManager.getRedisTimeWheel();
        this.deliverSupport = deliverSupport;
    }


    public void start() {
        log.info("启动时间轮刻度拨动器，启动时间：{}", new Date());
        skiper = new Thread(new Skiper(), "wheel_skiper");
        skiper.setDaemon(true);
        skiper.start();
    }


    public void stop() {
        log.info("停止时间轮刻度拨动器，停止时间：{}", new Date());
        skiperStop = true;
        skiper.interrupt();
    }



    class Skiper implements Runnable {

        @Override
        public void run() {

            while (!skiperStop) {

                coordinator.waitSkip();

                if (coordinator.isOffline()) {
                    log.info("节点已下线，不进行拨动");
                    return;
                }

                this.doSkipTimeWheel();

                if (coordinator.isOffline()) {
                    log.info("节点已下线，不进行拨动");
                    return;
                }

                coordinator.notifyRead();
            }
        }


        public void doSkipTimeWheel() {

            redisTimeWheel.waitExecuteTimePoint();

            boolean notCycleEndPoint = true;

            while (notCycleEndPoint && !Thread.currentThread().isInterrupted()) {

                try {

                    int targetSkipPoint = doSkipTimeWheelGrid();

                    if (redisTimeWheel.isCycleEndPoint(targetSkipPoint)) {
                        notCycleEndPoint = false;
                    }

                } catch (Exception e) {
                    log.error("拨动异常", e);
                }

                if (notCycleEndPoint) {
                    redisTimeWheel.waitExecuteTimePoint();
                }
            }
        }


        public int doSkipTimeWheelGrid() {

            long monitorStartTime = System.currentTimeMillis();

            long wheelStartTime = redisTimeWheel.getStartTime();
            int wheelTotalGrids  = redisTimeWheel.getTotalGrids();
            int startPoint = redisTimeWheel.getStartPoint();
            int lastSkipPoint = redisTimeWheel.getLastSkipPoint();
            int beforeSkipPoint = redisTimeWheel.calculateSkipPoint();

            long forwardSearchTime = redisTimeWheel.calculateForwardSearchTime(monitorStartTime);
            long backwardSearchTime = redisTimeWheel.calculateBackwardSearchTime(monitorStartTime);

            log.info("开始拨动时间轮，时间轮开始时间：{}, 当前索引点：{}, 上次索引点:{}，总索引数：{}",
                    wheelStartTime, beforeSkipPoint, lastSkipPoint, wheelTotalGrids);

            int targetSkipPoint = skipGrid(wheelStartTime, wheelTotalGrids, startPoint,
                    lastSkipPoint, beforeSkipPoint, monitorStartTime, forwardSearchTime, backwardSearchTime);

            int afterSkipPoint = redisTimeWheel.calculateSkipPoint();

            if (isSkipTimeout(wheelTotalGrids, beforeSkipPoint, afterSkipPoint)) {

                targetSkipPoint = skipGrid(wheelStartTime, wheelTotalGrids, startPoint,
                        beforeSkipPoint, afterSkipPoint, monitorStartTime, forwardSearchTime, backwardSearchTime);
            }

            redisTimeWheel.setLastSkipPoint(targetSkipPoint);

            long monitorEndTime = System.currentTimeMillis();

            log.info("拨动时间轮结束，操作开始时间：{}, 操作结束时间：{}, 拨动耗时：{}",
                    monitorStartTime, monitorEndTime,  monitorEndTime - monitorStartTime);

            return targetSkipPoint;
        }


        public int skipGrid(long wheelStartTime, int wheelTotalGrids, int startPoint,
                            int lastSkipPoint, int currentSkipPoint,
                            long skipTime, long forwardSearchTime, long backwardSearchTime) {

            if (currentSkipPoint - lastSkipPoint > 0) {

                for (int i = lastSkipPoint + 1; i <= currentSkipPoint; i++) {
                    handle(wheelStartTime, i, skipTime, forwardSearchTime, backwardSearchTime);
                }

                return currentSkipPoint;

            } else {

                if (lastSkipPoint == wheelTotalGrids) {

                    for (int i = startPoint; i <= currentSkipPoint; i++) {
                        handle(wheelStartTime, i, skipTime, forwardSearchTime, backwardSearchTime);
                    }

                    return currentSkipPoint;

                } else {

                    for (int i = lastSkipPoint + 1; i <= wheelTotalGrids; i++) {
                        handle(wheelStartTime, i, skipTime, forwardSearchTime, backwardSearchTime);
                    }

                    return wheelTotalGrids;
                }
            }
        }


        /**
         * 增加投递任务分发层，采用线程池提交分发任务，进一步保证不出现跳格的情况
         */
        public void handle(long wheelStartTime, int skipPoint, long skipTime,
                           long forwardSearchTime, long backwardSearchTime) {

            deliverSupport.submitDispatchTask(wheelStartTime, skipPoint, skipTime, forwardSearchTime, backwardSearchTime);
        }


        /**
         * 检查是否跳格，也就是跳动一次的执行时间超过了每个格子时间
         *
         *  例如： 1 2 3 4 5 ...  300
         *
         *  场景1： beforeSkipPoint: 5    afterSkipPoint: 5   不超时
         *  场景2： beforeSkipPoint: 5    afterSkipPoint: 6   超时
         *  场景3： beforeSkipPoint: 299  afterSkipPoint: 2   超时
         *  场景4： beforeSkipPoint: 300  afterSkipPoint: 2   不超时（例外情况）
         *
         *  以上场景4属于例外情况，因为最后一格执行完后，会执行预读操作。所以不执行超时补偿的逻辑
         */
        public boolean isSkipTimeout(int wheelTotalGrids, int beforeSkipPoint, int afterSkipPoint) {

            int diffSkipPoint = afterSkipPoint - beforeSkipPoint;

            if (diffSkipPoint > 0) {
                return true;
            }

            if (diffSkipPoint < 0 && beforeSkipPoint != wheelTotalGrids) {
                return true;
            }

            return false;
        }


    }



}
