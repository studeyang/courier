package com.github.open.courier.client.producer;

import com.github.open.courier.client.producer.sender.Sender;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.client.producer.transaction.TransactionMessageMapper;
import com.github.open.courier.core.exception.NotActiveTransactionException;
import com.github.open.courier.messaging.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.List;

/**
 * 事务消息发布器, 消息将在本地事务提交后发布到Producer
 *
 * @author Courier
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionMessagePublisher implements MessagePublisher {

    private final Sender sender;
    private final TransactionMessageMapper messageMapper;
    private final int partitionSize;

    @Override
    public void publish(Message message) {

        checkTransaction();

        SendMessage sendMessage = SendMessageConverter.convert(message);

        messageMapper.insert(sendMessage);

        registerCallBack(() -> sender.trySend(sendMessage));
    }

    @Override
    public void publish(Collection<? extends Message> messages) {

        checkTransaction();

        List<SendMessage> sendMessages = SendMessageConverter.convert(messages);

        ListUtils.partition(sendMessages, partitionSize).forEach(messageMapper::insertBatch);

        registerCallBack(() -> sender.trySend(sendMessages));
    }

    /**
     * 校验当前线程是否在事务中, 并初始化本地消息表
     */
    private static void checkTransaction() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new NotActiveTransactionException();
        }
    }

    /**
     * 注册事务提交后的回调器, 在事务提交后再真正发送消息
     */
    private static void registerCallBack(Runnable callback) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCommit() {
                callback.run();
            }

            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    log.warn("事务已回滚, kafka事务消息不会发送");
                }
            }
        });
    }
}
