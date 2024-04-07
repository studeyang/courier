package com.github.open.courier.client.producer.sender;

import com.github.open.courier.client.feign.ManagementClient;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.converter.SendFailMessageConverter;
import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.core.transport.SendFailMessage;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yanglulu
 */
@Slf4j
public class ReportableSender implements SenderDecorator {

    private Sender sender;
    private Reportable reporter;
    private ManagementClient managementClient;
    @Getter
    private int partitionSize;

    public ReportableSender(Sender sender,
                            Reportable reporter,
                            ManagementClient managementClient,
                            int partitionSize) {
        this.sender = sender;
        this.reporter = reporter;
        this.managementClient = managementClient;
        this.partitionSize = partitionSize;
    }

    @Override
    public MessageSendResult trySend(SendMessage message) {

        MessageSendResult result = sender.trySend(message);

        if (result.isSuccess()) {
            reporter.onSuccess(message);
        } else {
            reporter.onFail(message.addRetries(RetryableSender.RETRIES));
            report(message, result);
        }

        return result;
    }

    @Override
    public List<MessageSendResult> trySend(List<SendMessage> messages) {

        List<MessageSendResult> sendResults = sender.trySend(messages);

        Map<Boolean, List<SendMessage>> messagesMap = SendMessageConverter.classify(messages, sendResults);

        reporter.onSuccess(messagesMap.get(true));

        reporter.onFail(messagesMap.get(false));
        report(messagesMap.get(false), sendResults);

        return sendResults;
    }

    public void report(SendMessage message, MessageSendResult result) {

        SendFailMessage failMessage = SendFailMessageConverter.toFailMessage(message, result);

        try {
            managementClient.sendFail(failMessage);
            reporter.onReport(message);
        } catch (Exception e) {
            log.error("kafka提交发送失败信息失败, message:{}", message, e);
        }
    }

    /**
     * 将一批SendMessage, 报告给management
     */
    public void report(List<SendMessage> messages, List<MessageSendResult> results) {

        // 待报告的失败消息
        List<SendFailMessage> failMessages = SendFailMessageConverter.toFailMessages(messages, results);

        // 成功报告的ids
        Set<String> reportedIds = Sets.newHashSetWithExpectedSize(messages.size());

        // 分批
        ListUtils.partition(failMessages, partitionSize)
                .forEach(ms -> {
                    List<String> ids = ms.stream().map(SendFailMessage::getMessageId).collect(Collectors.toList());
                    try {
                        managementClient.sendFail(ms);
                        reportedIds.addAll(ids);
                    } catch (Exception e) {
                        log.error("kafka提交发送失败信息失败, messageIds:{}", ids, e);
                    }
                });

        // 成功报告的消息
        List<SendMessage> reportedMessages = messages.stream().filter(m -> reportedIds.contains(m.getMessageId())).collect(Collectors.toList());
        reporter.onReport(reportedMessages);
    }

}
