package com.github.open.courier.delay.core.support.delive;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * 延迟消息投递任务
 *
 * @author wangyonglin
 */
@Slf4j
public class DeliverTask implements Runnable {

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
     * 延迟消息ID集合
     */
    final List<String> messageIds;

    /**
     * 向前搜索补偿的时间
     */
    final long forwardSearchTime;

    /**
     * 向后搜索补偿的时间
     */
    final long backwardSearchTime;


    final DeliverSupport deliverSupport;


    public DeliverTask(DeliverSupport deliverSupport,
                       List<String> messageIds,
                       long forwardSearchTime,
                       long backwardSearchTime,
                       long wheelStartTime,
                       int skipPoint,
                       long skipTime) {

        this.messageIds = messageIds;
        this.forwardSearchTime = forwardSearchTime;
        this.backwardSearchTime = backwardSearchTime;

        this.wheelStartTime = wheelStartTime;
        this.skipPoint = skipPoint;
        this.skipTime = skipTime;

        this.deliverSupport = deliverSupport;
    }


    @Override
    public void run() {

        long monitorStartTime = System.currentTimeMillis();

        log.info("投递任务信息：[wheelStartTime:{}, skipPoint:{}, skipTime:{}, size:{}, forwardSearchTime:{}, backwardSearchTime:{}, messageIds:{}]",
                wheelStartTime, skipPoint, skipTime, messageIds.size(), forwardSearchTime, backwardSearchTime, messageIds);

        Date startDeliveTime = new Date();

        DeliverResult deliverResult;
        try {
            deliverResult = deliverSupport.trySend(messageIds, forwardSearchTime, backwardSearchTime);
        } catch (Exception e) {
            log.error("延迟消息投递任务尝试发送失败", e);
            deliverSupport.retry(messageIds, forwardSearchTime, backwardSearchTime, wheelStartTime, skipPoint, skipTime);
            return;
        }

        Date endDeliveTime = new Date();

        deliverSupport.handleSuccess(deliverResult.getSuccessMessages(), forwardSearchTime, backwardSearchTime, startDeliveTime, endDeliveTime);

        deliverSupport.handleFail(deliverResult.getFailMessages(), forwardSearchTime, backwardSearchTime, wheelStartTime, skipPoint, skipTime);

        long monitorEndTime = System.currentTimeMillis();

        log.info("消息投递完成，操作开始时间：{}, 操作结束时间：{}, 投递耗时：{}", monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


}
