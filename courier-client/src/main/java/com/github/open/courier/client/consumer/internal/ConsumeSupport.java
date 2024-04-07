package com.github.open.courier.client.consumer.internal;

import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.client.consumer.internal.ConsumeReporters.ConsumeReporter;
import com.github.open.courier.core.transport.MessageConsumeTime;
import com.github.open.courier.messaging.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.github.open.courier.core.converter.ConsumeFailMessageConverter.toConsumeFailMessage;
import static com.github.open.courier.core.converter.ExceptionConverter.getCause;

/**
 * 消费消息support
 *
 * @author Courier
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ConsumeSupport {

    final ExecutorService asyncExecutor;
    final ExecutorService syncExecutor;
    final ExecutorService retryExecutor;
    final ConsumeReporter reporter;
    final MessageHandlerContainer handlerContainer;
    final boolean enableRetry;

    /**
     * 消费消息
     */
    public void consume(ConsumeMessage consumeMessage) {
        execute(new ConsumeTask(this, consumeMessage, new Date()));
    }

    /**
     * 消费消息
     */
    public void consume(List<ConsumeMessage> consumeMessages) {
        if (CollectionUtils.isNotEmpty(consumeMessages)) {
            Date receiveStamp = new Date();
            consumeMessages.forEach(m -> execute(new ConsumeTask(this, m, receiveStamp)));
        }
    }

    /**
     * 提交到线程池, 如果失败, 则通过management重试
     */
    public void execute(ConsumeTask task) {
        try {
            if (task.isRetry()) {
                retryExecutor.execute(task);
            } else if (task.isSequence()) {
                syncExecutor.execute(task);
            } else {
                asyncExecutor.execute(task);
            }
        } catch (Exception e) {
            fail(task.getConsumeMessage(), getCause(e), true);
        }
    }

    /**
     * 获取MessageHandler
     */
    MessageHandler getHandler(Message message) {
        return handlerContainer.getHandler(message);
    }

    /**
     * 消费成功
     */
    void success(ConsumeTask task) {

        ConsumeMessage consumeMessage = task.getConsumeMessage();

        long waitCost = task.getWaitCost();
        long consumeCost = task.getConsumeCost();

        log.info("kafka消费成功, cid:{}, mid:{}, 等待耗时:{} ms, 消费耗时:{} ms",
                consumeMessage.getId(), consumeMessage.getMessageId(), waitCost, consumeCost);

        reporter.reportSuccess(
                new MessageConsumeTime(consumeMessage.getId(),
                        task.getReceiveStamp(),
                        new Date(task.getAfterConsumeStamp()),
                        waitCost + consumeCost,
                        consumeMessage.getPollTime(),
                        consumeMessage.getPollTime())
        );
    }

    /**
     * 消费失败
     */
    void fail(ConsumeMessage consumeMessage, String cause, boolean retryByManagement) {

        log.error("kafka消费失败, cid:{}, message:{}, cause:{}", consumeMessage.getId(), consumeMessage.getContent(), cause);

        if (Boolean.TRUE.equals(consumeMessage.getNeedRepush())) {
            reporter.reportFail(toConsumeFailMessage(consumeMessage.setNeedRepush(retryByManagement), cause));
        }
    }

    /**
     * 重试
     */
    void retry(ConsumeTask task, Exception e) {

        ConsumeMessage consumeMessage = task.getConsumeMessage();

        if (enableRetry && Boolean.TRUE.equals(consumeMessage.getNeedRepush())) {
            log.warn("kafka首次消费失败, 开始重试, cid:{}, message:{}", consumeMessage.getId(), consumeMessage.getContent(), e);
            execute(new RetryConsumeTask(task));
        } else {
            fail(consumeMessage, getCause(e), false);
        }
    }

    @PreDestroy
    public void destroy() {
        asyncExecutor.shutdown();
        syncExecutor.shutdown();
        retryExecutor.shutdown();
    }
}
