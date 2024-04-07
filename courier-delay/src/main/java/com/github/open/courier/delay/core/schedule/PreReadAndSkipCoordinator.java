package com.github.open.courier.delay.core.schedule;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 消息预读和时间轮刻度拨动操作间的协调者
 *
 * @author wangyonglin
 */
@Slf4j
public class PreReadAndSkipCoordinator {

    /**
     * 节点是否在线
     */
    private volatile boolean isOnline;

    /**
     * 时间轮当前状态
     *    预读状态（READ）
     *    拨动状态（SKIP）
     */
    private TimeWheelStatus timeWheelStatus;

    /**
     * 预读操作和拨动操作的协调锁
     */
    private final ReentrantLock coordinaterLock;

    /**
     * 预读条件
     */
    private final Condition readCondition;

    /**
     * 拨动条件
     */
    private final Condition skipCondition;


    public PreReadAndSkipCoordinator() {

        this.isOnline = true;
        this.timeWheelStatus = TimeWheelStatus.READ;

        this.coordinaterLock = new ReentrantLock();
        this.readCondition = coordinaterLock.newCondition();
        this.skipCondition = coordinaterLock.newCondition();
    }


    /**
     * 等待预读：
     *    1.自旋是防止过早唤醒和错过唤醒信号;
     *    2.节点状态的检查.是防止服务下线时，从wait状态唤醒，无法退出自旋;
     */
    public void waitRead() {
        coordinaterLock.lock();
        try {
            while (!canRead()) {
                long monitorStartTime = System.currentTimeMillis();
                log.info("预读条件不满足，进入[wait]状态, 沉睡时间：{}", monitorStartTime);
                readCondition.await();
                long monitorEndTime = System.currentTimeMillis();
                log.info("预读线程被唤醒, 唤醒时间：{}, 等待耗时：{}", monitorEndTime, monitorEndTime - monitorStartTime);
            }
        } catch (InterruptedException e) {
            log.warn("预读线程wait状态被打断", e);
            Thread.currentThread().interrupt();
        } finally {
            coordinaterLock.unlock();
        }
    }


    /**
     * 等待拨动
     *    1.自旋是防止过早唤醒和错过唤醒信号;
     *    2.节点状态的检查.是防止服务下线时，从wait状态唤醒，无法退出自旋;
     */
    public void waitSkip() {
        coordinaterLock.lock();
        try {
            while (!canSkip()) {
                long monitorStartTime = System.currentTimeMillis();
                log.info("拨动条件不满足，进入[wait]状态, 沉睡时间：{}", monitorStartTime);
                skipCondition.await();
                long monitorEndTime = System.currentTimeMillis();
                log.info("拨动线程被唤醒, 唤醒时间：{}, 等待耗时：{}", monitorEndTime, monitorEndTime - monitorStartTime);
            }
        } catch (InterruptedException e) {
            log.warn("拨动线程wait状态被打断", e);
            Thread.currentThread().interrupt();
        } finally {
            coordinaterLock.unlock();
        }
    }



    /**
     * 通知预读线程进行预读
     */
    public void notifyRead() {
        long monitorStartTime = System.currentTimeMillis();
        log.info("设置协调器状态为[READ],并通知消息预读线程执行, 当前时间：{}", monitorStartTime);
        coordinaterLock.lock();
        try {
            timeWheelStatus = TimeWheelStatus.READ;
            readCondition.signalAll();
        } finally {
            coordinaterLock.unlock();
        }
    }


    /**
     * 通知拨动线程进行拨动
     */
    public void notifySkip() {
        long monitorStartTime = System.currentTimeMillis();
        log.info("设置协调器状态为[SKIP],并通知时间轮刻度拨动线程执行, 当前时间：{}", monitorStartTime);
        coordinaterLock.lock();
        try {
            timeWheelStatus = TimeWheelStatus.SKIP;
            skipCondition.signalAll();
        } finally {
            coordinaterLock.unlock();
        }
    }


    /**
     * 是否可以预读
     */
    public boolean canRead() {
        return isOnline() && isRead();
    }


    /**
     * 是否可以拨动
     */
    public boolean canSkip() {
        return isOnline() && isSkip();
    }


    /**
     * 是否是预读状态
     */
    public boolean isRead() {
        return timeWheelStatus == TimeWheelStatus.READ;

    }

    /**
     * 是否是拨动状态
     */
    public boolean isSkip() {
        return timeWheelStatus == TimeWheelStatus.SKIP;
    }


    /**
     * 节点是否在线
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * 节点是否下线
     */
    public boolean isOffline() {
        return !isOnline;
    }

    /**
     * 下线节点
     */
    public void offineNode() {
        long monitorStartTime = System.currentTimeMillis();
        log.info("设置消息预读和时间轮刻度拨动操做协调器的节点状态为下线状态, 当前时间：{}", monitorStartTime);
        isOnline = false;
    }


    private enum TimeWheelStatus {
        /**
         * 预读消息状态
         */
        READ,
        /**
         * 时间轮刻度拨动状态
         */
        SKIP
    }



}
