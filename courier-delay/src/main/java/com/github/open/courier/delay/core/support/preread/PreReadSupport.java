package com.github.open.courier.delay.core.support.preread;

import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.core.transport.MessageOperationCondition;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 延迟消息预读支持处理器
 *
 * @author wangyonglin
 */
@Slf4j
public class PreReadSupport {

    private final int partitionSize;

    private final DelayMessageMapper delayMessageMapper;

    private final ExecutorService markReadedExecutor;


    public PreReadSupport(ScheduleManager scheduleManager) {

        CourierServerProperties.DelayProperties.PreReadProperties preReadProperties =
                scheduleManager.getCourierDelayProperties().getPreRead();

        this.markReadedExecutor = preReadProperties.getMarkReaded().create();

        this.partitionSize = preReadProperties.getPartitionSize();
        this.delayMessageMapper = scheduleManager.getDelayMessageMapper();
    }


    /**
     * 提交延迟消息的预读状态标记任务
     */
    public void submitMarkReadedTask(List<DelayMessage> delayMessages, long forwardSearchTime, long backwardSearchTime) {

        long monitorStartTime = System.currentTimeMillis();

        List<String> messageIds = delayMessages.stream().map(DelayMessage::getMessageId).collect(Collectors.toList());

        // 拆分一下标记任务任务，防止单格消息量过多，导致数据库查询卡死
        Lists.partition(messageIds, partitionSize).forEach(ms -> {
            try {
                markReadedExecutor.execute(new MarkReadedTask(this, ms, forwardSearchTime, backwardSearchTime));
            } catch (Exception e) {
                log.error("延迟消息预读状态标记任务提交线程池失败, messageIds:{}", ms, e);
            }
        });

        long monitorEndTime = System.currentTimeMillis();

        log.info("提交延迟消息的预读状态标记任务，操作开始时间：{}，操作结束时间：{}, 消息size：{}， 提交耗时：{}",
                monitorStartTime, monitorEndTime, messageIds.size(), monitorEndTime - monitorStartTime);
    }


    /**
     * 标记预读状态
     */
    public void markReaded(List<String> messageIds, long forwardSearchTime, long backwardSearchTime) {

        MessageOperationCondition condition = MessageOperationCondition.builder()
                .messageIds(messageIds).startTime(forwardSearchTime).endTime(backwardSearchTime).build();

        delayMessageMapper.updateReadedByIds(condition);
    }


    /**
     * 停止延迟消息预读支持处理器
     */
    public void stop() {

        long monitorStartTime = System.currentTimeMillis();

        log.info("停止延迟消息预读支持处理器，当前时间：{}", monitorStartTime);

        markReadedExecutor.shutdown();
    }


}
