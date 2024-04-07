package com.github.open.courier.delay.core.support.delive;

import com.github.open.courier.core.converter.DelayMessageConverter;
import com.github.open.courier.core.converter.SendFailMessageConverter;
import com.github.open.courier.core.transport.*;
import com.github.open.courier.delay.client.ManagementClient;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 消息发送结果报告器
 *
 * @author wangyonglin
 */
@Slf4j
public class DeliverReporter {

    private final int partitionSize;

    private final ManagementClient managementClient;
    private final DelayMessageMapper delayMessageMapper;
    private final ExecutorService reportExecutor;


    public DeliverReporter(int partitionSize,
                           ManagementClient managementClient,
                           DelayMessageMapper delayMessageMapper,
                           ExecutorService reportExecutor) {

        this.managementClient = managementClient;
        this.delayMessageMapper = delayMessageMapper;
        this.partitionSize = partitionSize;
        this.reportExecutor = reportExecutor;
    }


    /**
     * 报告发送失败的消息
     */
    public void reportSendFail(List<String> messageIds, long forwardSearchTime, long backwardSearchTime, String reason) {

        if (CollectionUtils.isEmpty(messageIds)) {
            log.info("没有需要报告发送失败的消息");
            return;
        }

        try {
            reportExecutor.execute(() -> {

                MessageOperationCondition condition = MessageOperationCondition.builder()
                        .messageIds(messageIds).startTime(forwardSearchTime).endTime(backwardSearchTime).build();

                List<DelayMessage> delayMessages = delayMessageMapper.listNeedSendByMessageIds(condition);

                if (CollectionUtils.isEmpty(delayMessages)) {
                    return;
                }

                List<SendMessage> failMessages = DelayMessageConverter.toSendMessages(delayMessages);

                List<MessageSendResult> sendResults = messageIds.stream()
                        .map(messageId -> MessageSendResult.error(messageId, reason)).collect(Collectors.toList());

                report(SendFailMessageConverter.toFailMessages(failMessages, sendResults));
            });
        } catch (Exception e) {
            log.error("消息提交报告线程池失败，messageIds:{}", messageIds, e);
        }
    }


    /**
     * 报告发送失败的消息
     */
    public void reportSendFail(List<SendMessage> messages, List<MessageSendResult> sendResults) {

        if (CollectionUtils.isEmpty(messages)) {
            log.info("没有需要报告发送失败的消息");
            return;
        }

        try {
            reportExecutor.execute(() -> report(SendFailMessageConverter.toFailMessages(messages, sendResults)));
        } catch (Exception e) {
            List<String> messageIds = messages.stream().map(SendMessage::getMessageId).collect(Collectors.toList());
            log.error("消息提交报告线程池失败, messageIds:{}", messageIds, e);
        }
    }


    private void report(List<SendFailMessage> messages) {

        ListUtils.partition(messages, partitionSize).forEach(ms -> {

            List<String> messageIds = ms.stream()
                    .map(SendFailMessage::getMessageId).collect(Collectors.toList());

            try {
                managementClient.sendFail(ms);
            } catch (Exception e) {
                log.error("报告发送失败信息失败, messageIds:{}", messageIds, e);
            }
        });
    }

}
