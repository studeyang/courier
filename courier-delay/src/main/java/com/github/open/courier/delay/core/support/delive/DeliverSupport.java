package com.github.open.courier.delay.core.support.delive;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.delay.biz.MessageBizService;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import com.github.open.courier.commons.support.CourierServerProperties;
import com.github.open.courier.core.converter.DelayMessageConverter;
import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.core.transport.MessageOperationCondition;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.delay.core.schedule.RedisTimeWheel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.github.open.courier.core.converter.ExceptionConverter.getCause;


/**
 * 延迟消息投递支持处理器
 *
 * @author wangyonglin
 */
@Slf4j
public class DeliverSupport {

    private final RedisTimeWheel redisTimeWheel;
    private final MessageBizService messageBizService;
    private final DelayMessageMapper delayMessageMapper;

    private final DeliverReporter deliverReporter;

    private final int partitionSize;

    private final ExecutorService dispatchExecutor;
    private final ExecutorService asyncExecutor;
    private final ExecutorService retryExecutor;
    private final ExecutorService reportExecutor;


    public DeliverSupport(ScheduleManager scheduleManager) {

        CourierServerProperties.DelayProperties.DeliverProperties deliverProperties =
                scheduleManager.getCourierDelayProperties().getDeliver();

        this.partitionSize = deliverProperties.getPartitionSize();
        this.dispatchExecutor = deliverProperties.getDispatch().create();
        this.asyncExecutor = deliverProperties.getAsync().create();
        this.retryExecutor = deliverProperties.getRetry().create();
        this.reportExecutor = deliverProperties.getReporter().create();

        this.redisTimeWheel = scheduleManager.getRedisTimeWheel();
        this.messageBizService = scheduleManager.getMessageBizService();
        this.delayMessageMapper = scheduleManager.getDelayMessageMapper();

        this.deliverReporter = new DeliverReporter(partitionSize,
                scheduleManager.getManagementClient(), delayMessageMapper, reportExecutor);
    }


    /**
     * 提交延时消息分发任务到任务池
     */
    public void submitDispatchTask(long wheelStartTime, int skipPoint,
                                   long skipTime, long forwardSearchTime, long backwardSearchTime) {

        long monitorStartTime = System.currentTimeMillis();

        try {
            dispatchExecutor.execute(new DispatchTask(this, redisTimeWheel,
                    partitionSize, wheelStartTime, skipPoint, skipTime, forwardSearchTime, backwardSearchTime)
            );
        } catch (Exception e) {
            log.error("延迟消息分发任务提交线程池失败", e);
        }

        long monitorEndTime = System.currentTimeMillis();

        log.info("提交延时消息分发任务, 操作开始时间：{}, 操作结束时间：{}, 提交耗时：{}", monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 提交延时消息投递任务到任务池
     */
    public void submitDeliverTask(List<String> messageIds, long wheelStartTime,
                                  int skipPoint, long skipTime, long forwardSearchTime, long backwardSearchTime) {

        long monitorStartTime = System.currentTimeMillis();

        try {
            asyncExecutor.execute(new DeliverTask(this, messageIds,
                    forwardSearchTime, backwardSearchTime, wheelStartTime, skipPoint, skipTime)
            );
        } catch (Exception e) {
            log.error("延迟消息投递任务提交线程池失败", e);
            retry(messageIds, forwardSearchTime, backwardSearchTime, wheelStartTime, skipPoint, skipTime);
        }

        long monitorEndTime = System.currentTimeMillis();

        log.info("提交延时消息投递任务, 操作开始时间：{}, 操作结束时间：{}, 提交耗时：{}", monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 重试投递任务
     */
    public void retry(List<String> messageIds, long forwardSearchTime,
                      long backwardSearchTime, long wheelStartTime, int skipPoint, long skipTime) {

        if (CollectionUtils.isEmpty(messageIds)) {
            log.info("需要重试消息ID集合为空，不进行重试逻辑");
            return;
        }

        try {
            retryExecutor.execute(new RetryDeliverTask(this, messageIds,
                    forwardSearchTime, backwardSearchTime, wheelStartTime, skipPoint, skipTime)
            );
        } catch (Exception e) {
            log.error("延迟消息重试投递任务提交线程池失败, messageIds:{}", messageIds, e);
            reportSendFail(messageIds, forwardSearchTime, backwardSearchTime, getCause(e));
        }
    }


    /**
     * 尝试发送消息
     */
    public DeliverResult trySend(List<String> messageIds, long forwardSearchTime, long backwardSearchTime) {

        long monitorStartTime = System.currentTimeMillis();

        MessageOperationCondition condition = MessageOperationCondition.builder()
                .messageIds(messageIds).startTime(forwardSearchTime).endTime(backwardSearchTime).build();

        List<DelayMessage> delayMessages = delayMessageMapper.listNeedSendByMessageIds(condition);

        List<SendMessage> sendMessages = DelayMessageConverter.toSendMessages(delayMessages);

        long monitorSelectTime = System.currentTimeMillis();

        List<MessageSendResult> sendResults = messageBizService.send(sendMessages);

        long monitorEndTime = System.currentTimeMillis();

        log.info("尝试投递延迟消息，消息size：{}, 投递开始时间：{}, 查询耗时：{}, 发送耗时：{}， 总耗时：{}",
                messageIds.size(), monitorStartTime, monitorSelectTime - monitorStartTime,
                monitorEndTime - monitorSelectTime, monitorEndTime - monitorStartTime);

        Map<Boolean, List<SendMessage>> resultMap = SendMessageConverter.classify(sendMessages, sendResults);

        return new DeliverResult(sendResults, resultMap.get(true), resultMap.get(false));
    }


    /**
     * 处理发送成功消息
     */
    public void handleSuccess(List<SendMessage> sendMessages, long forwardSearchTime,
                              long backwardSearchTime, Date startDeliveTime, Date endDeliveTime) {

        long monitorStartTime = System.currentTimeMillis();

        if (CollectionUtils.isNotEmpty(sendMessages)) {

            List<String> messageIds = sendMessages.stream().map(SendMessage::getMessageId).collect(Collectors.toList());

            MessageOperationCondition condition = MessageOperationCondition.builder()
                    .messageIds(messageIds).startDeliveTime(startDeliveTime).endDeliveTime(endDeliveTime)
                    .startTime(forwardSearchTime).endTime(backwardSearchTime).build();

            delayMessageMapper.updateSendedByIds(condition);
        }

        long monitorEndTime = System.currentTimeMillis();

        log.info("处理投递成功延迟消息，消息size：{}, 操作开始时间：{}, 操作结束耗时：{}, 操作耗时：{}，",
                sendMessages.size(), monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }


    /**
     * 处理发送失败消息
     */
    public void handleFail(List<SendMessage> sendMessages, long forwardSearchTime,
                           long backwardSearchTime, long wheelStartTime, int skipPoint, long skipTime) {

        if (CollectionUtils.isNotEmpty(sendMessages)) {

            log.info("存在投递失败的延迟消息，进行重试投递逻辑，消息size：{}", sendMessages.size());

            List<String> messageIds = sendMessages.stream()
                    .map(SendMessage::getMessageId).collect(Collectors.toList());

            retry(messageIds, forwardSearchTime, backwardSearchTime, wheelStartTime, skipPoint, skipTime);
        }
    }


    /**
     * 报告发送失败的消息
     */
    void reportSendFail(List<String> sendFailMessageIds,
                        long forwardSearchTime, long backwardSearchTime, String failReason) {

        deliverReporter.reportSendFail(sendFailMessageIds, forwardSearchTime, backwardSearchTime, failReason);
    }


    /**
     * 报告发送失败的消息
     */
    void reportSendFail(List<SendMessage> sendFailMessages, List<MessageSendResult> sendResults) {

        deliverReporter.reportSendFail(sendFailMessages, sendResults);
    }


    /**
     * 停止延迟消息投递支持处理器
     */
    public void stop() {

        long monitorStartTime = System.currentTimeMillis();

        log.info("停止延迟消息投递支持处理器，当前时间：{}", monitorStartTime);

        dispatchExecutor.shutdown();

        asyncExecutor.shutdown();

        retryExecutor.shutdown();

        reportExecutor.shutdown();
    }

}
