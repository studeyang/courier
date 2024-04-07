package com.github.open.courier.client.producer.sender;

import com.github.open.courier.client.feign.ProducerClient;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.client.producer.transaction.TransactionMessageMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 事务消息的sender
 *
 * @author yanglulu
 */
public class TransactionProducerSender extends ProducerSender implements Reportable {

    private final TransactionMessageMapper messageMapper;

    public TransactionProducerSender(ProducerClient producerClient,
                                     TransactionMessageMapper messageMapper,
                                     int partitionSize) {
        super(producerClient, partitionSize);
        this.messageMapper = messageMapper;
    }

    /**
     * 发送成功后删除本地的消息
     */
    @Override
    public void onSuccess(SendMessage successMessage) {
        delete(successMessage);
    }

    /**
     * 发送成功后删除本地的消息
     */
    @Override
    public void onSuccess(List<SendMessage> successMessages) {
        delete(successMessages);
    }

    /**
     * 重试发送失败时, 更新数据库(retries字段)
     */
    @Override
    public void onFail(SendMessage retryFailMessage) {
        messageMapper.updateRetries(retryFailMessage);
    }

    /**
     * 重试发送失败时, 更新数据库(retries字段)
     */
    @Override
    public void onFail(List<SendMessage> retryFailMessages) {
        if (CollectionUtils.isNotEmpty(retryFailMessages)) {
            ListUtils.partition(retryFailMessages, getPartitionSize()).forEach(messageMapper::updateRetriesBatch);
        }
    }

    /**
     * 报告后, 删除本地记录
     */
    @Override
    public void onReport(SendMessage retryFailMessage) {
        delete(retryFailMessage);
    }

    /**
     * 报告后, 删除本地记录
     */
    @Override
    public void onReport(List<SendMessage> retryFailMessages) {
        delete(retryFailMessages);
    }

    private void delete(SendMessage message) {
        messageMapper.delete(message.getMessageId());
    }

    private void delete(List<SendMessage> messages) {
        if (CollectionUtils.isNotEmpty(messages)) {
            ListUtils.partition(messages, getPartitionSize())
                    .forEach(ms -> {
                        List<String> messageIds = ms.stream().map(SendMessage::getMessageId).collect(Collectors.toList());
                        messageMapper.deleteBatch(messageIds);
                    });
        }
    }
}
