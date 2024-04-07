package com.github.open.courier.delay.core.schedule;

import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.delay.core.support.delive.DeliverSupport;
import com.github.open.courier.delay.core.support.preread.PreReadSupport;
import lombok.extern.slf4j.Slf4j;

/**
 * 延迟消息调度器
 *
 * @author wangyonglin
 */
@Slf4j
public class DelayMessageScheduler {

    private final ScheduleManager scheduleManager;
    private final RedisTimeWheel redisTimeWheel;


    private PreReadAndSkipCoordinator coordinator;
    private MessagePreReader messagePreReader;
    private TimeWheelGridSkiper timeWheelGridSkiper;
    private PreReadSupport preReadSupport;
    private DeliverSupport deliverSupport;


    public DelayMessageScheduler(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
        this.redisTimeWheel = scheduleManager.getRedisTimeWheel();
    }


    /**
     *     时间轮处理启动时，需要重置时间轮的状态信息，这里是防止在
     *  发布时滚动更新或者突然宕机导致 主从切换时，原有的Redis时间
     *  轮状态信息导致计算位置的不准确
     *
     *  所以现在发布上线或者突然宕机，导致主从切换时会带来一个误差，假
     *  这个时候时间轮会从位置0开始拨动，已经预读进时间轮的只能等待刻度
     *  推进到对应位置，这个消息才会随新预读消息一起处理。
     *
     */
    public void start() {

        log.info("启动延迟消息调度器, 节点IP：{}", scheduleManager.getNodeIpAddr());

        this.coordinator = new PreReadAndSkipCoordinator();

        this.preReadSupport = new PreReadSupport(scheduleManager);
        this.messagePreReader = new MessagePreReader(coordinator, scheduleManager, preReadSupport);

        this.deliverSupport = new DeliverSupport(scheduleManager);
        this.timeWheelGridSkiper = new TimeWheelGridSkiper(coordinator, scheduleManager, deliverSupport);

        redisTimeWheel.reset();
        messagePreReader.start();
        timeWheelGridSkiper.start();
    }


    public void stop() {

        log.info("停止延迟消息调度器, 节点IP：{}", scheduleManager.getNodeIpAddr());

        coordinator.offineNode();

        messagePreReader.stop();
        preReadSupport.stop();

        timeWheelGridSkiper.stop();
        deliverSupport.stop();
    }


}
