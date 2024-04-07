package com.github.open.courier.delay.core.support.delive;

import com.github.open.courier.delay.core.schedule.RedisTimeWheel;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 延迟消息分发任务
 *
 * @author wangyonglin
 */
@Slf4j
public class DispatchTask implements Runnable {

    /**
     * 每批处理的消息大小
     */
    private final int partitionSize;

    /**
     * 时间轮开始时间：任务对应的时间轮信息，方便监控
     */
    final long wheelStartTime;
    /**
     * 任务所属时间轮索引：任务对应的时间轮信息，方便监控
     */
    final int skipPoint;
    /**
     * 拨动任务对应索引的时间：任务对应的时间轮信息，方便监控
     */
    final long skipTime;


    /**
     * 向前搜索补偿的时间
     */
    final long forwardSearchTime;

    /**
     * 向后搜索补偿的时间
     */
    final long backwardSearchTime;

    final RedisTimeWheel redisTimeWheel;
    final DeliverSupport deliverSupport;


    public DispatchTask(DeliverSupport deliverSupport,
                        RedisTimeWheel redisTimeWheel,
                        int partitionSize,
                        long wheelStartTime,
                        int skipPoint,
                        long skipTime,
                        long forwardSearchTime,
                        long backwardSearchTime) {

        this.wheelStartTime = wheelStartTime;
        this.skipPoint = skipPoint;
        this.skipTime = skipTime;

        this.forwardSearchTime = forwardSearchTime;
        this.backwardSearchTime = backwardSearchTime;

        this.partitionSize = partitionSize;
        this.redisTimeWheel = redisTimeWheel;
        this.deliverSupport = deliverSupport;
    }


    @Override
    public void run() {

        long monitorStartTime = System.currentTimeMillis();

        log.info("分发任务信息：[wheelStartTime:{}, skipPoint:{}, skipTime:{}, forwardSearchTime:{}, backwardSearchTime:{}]",
                wheelStartTime, skipPoint, skipTime, forwardSearchTime, backwardSearchTime);

        List<String> messageIds = redisTimeWheel.poll(skipPoint);

        if (CollectionUtils.isEmpty(messageIds)) {
            return;
        }

        // 拆分投递任务，防止单个任务消息量过多，导致数据库查询卡死
        Lists.partition(messageIds, partitionSize).forEach(ids -> {
            deliverSupport.submitDeliverTask(ids, wheelStartTime, skipPoint, skipTime, forwardSearchTime, backwardSearchTime);
        });

        redisTimeWheel.delete(skipPoint, messageIds);

        long monitorEndTime = System.currentTimeMillis();

        log.info("分发任务执行完成，操作开始时间：{}, 操作结束时间：{}, 分发耗时：{}", monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


}
