package com.github.open.courier.delay.core.support.preread;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * 延迟消息预读状态标记任务
 */
@Slf4j
public class MarkReadedTask implements Runnable {


    final PreReadSupport preReadSupport;

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



    public MarkReadedTask(PreReadSupport preReadSupport,
                          List<String> messageIds,
                          long forwardSearchTime,
                          long backwardSearchTime) {

        this.messageIds = messageIds;
        this.forwardSearchTime = forwardSearchTime;
        this.backwardSearchTime = backwardSearchTime;

        this.preReadSupport = preReadSupport;
    }


    @Override
    public void run() {

        long monitorStartTime = System.currentTimeMillis();

        log.info("预读状态标记任务任务信息：[messageIds:{}, forwardSearchTime:{}, backwardSearchTime:{}]",
                messageIds, forwardSearchTime, backwardSearchTime);

        if (CollectionUtils.isEmpty(messageIds)) {
            return;
        }

        preReadSupport.markReaded(messageIds, forwardSearchTime, backwardSearchTime);

        long monitorEndTime = System.currentTimeMillis();

        log.info("预读状态标记任务完成，操作开始时间：{}, 操作结束时间：{}, 标记耗时：{}",
                monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }



}
