package com.github.open.courier.client.producer.sender;

import com.github.open.courier.core.support.Retryable;
import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.converter.SendMessageConverter;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.rholder.retry.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author yanglulu
 */
@Slf4j
public class RetryableSender implements SenderDecorator {

    public static final int RETRIES = 3;

    private Sender sender;
    private final Retryer<MessageSendResult> singleRetryer;

    public RetryableSender(Sender sender) {
        this.sender = sender;
        this.singleRetryer = new SingleRetryer().build();
    }

    @Override
    @SneakyThrows
    public MessageSendResult trySend(SendMessage message) {

        MessageSendResult result = sender.trySend(message);

        if (result.isSuccess()) {
            return result;
        }

        try {
            return singleRetryer.call(() -> sender.trySend(message));
        } catch (RetryException e) {
            log.error("kafka重试发送消息失败, 失败原因: {}", result.getReason(), e);
            return (MessageSendResult) e.getLastFailedAttempt().getResult();
        }
    }

    @Override
    public List<MessageSendResult> trySend(List<SendMessage> messages) {

        List<MessageSendResult> sendResults = sender.trySend(messages);

        Map<Boolean, List<SendMessage>> resultMap = SendMessageConverter.classify(messages, sendResults);

        // 重试发送失败的消息
        Set<String> retrySuccess = retry(resultMap.get(false));

        for (MessageSendResult result : sendResults) {
            if (retrySuccess.contains(result.getMessageId())) {
                result.setSuccess(true);
            }
        }
        return sendResults;
    }

    /**
     * 重试发送一批SendMessage
     *
     * @param failMessages 一批发送失败的消息
     * @return 重试成功的 messageId 集合
     */
    @SneakyThrows
    private Set<String> retry(List<SendMessage> failMessages) {

        if (CollectionUtils.isEmpty(failMessages)) {
            return Collections.emptySet();
        }

        MultipleRetryerContext context = new MultipleRetryerContext(sender, failMessages);

        try {
            context.retry();
        } catch (RetryException e) {
            log.error("kafka重试发送消息失败, messageIds:{}",
                    failMessages.stream().map(SendMessage::getMessageId).collect(Collectors.toList()), e);
        }

        return context.getRetrySuccessMessages().stream()
                .map(SendMessage::getMessageId)
                .collect(Collectors.toSet());
    }

    /**
     * 单个消息的重试策略
     */
    static class SingleRetryer implements Retryable<MessageSendResult> {

        @Override
        public boolean apply(MessageSendResult result) {
            return !result.isSuccess();
        }

        @Override
        public StopStrategy getStopStrategy() {
            return StopStrategies.stopAfterAttempt(RETRIES);
        }

        @Override
        public WaitStrategy getWaitStrategy() {
            // 1s 2s 4s 8s 16s 32s 60s 60s 60s ...
            return WaitStrategies.exponentialWait(500, 1, TimeUnit.MINUTES);
        }
    }

    /**
     * 一批消息的重试策略
     */
    @Getter
    static class MultipleRetryerContext implements Retryable<List<MessageSendResult>> {

        private final Sender sender;
        /**
         * 待重试的消息
         */
        private List<SendMessage> failMessages;
        private final List<SendMessage> retrySuccessMessages;

        public MultipleRetryerContext(Sender sender, List<SendMessage> failMessages) {
            this.sender = sender;
            this.failMessages = failMessages;
            this.retrySuccessMessages = Lists.newArrayListWithCapacity(failMessages.size());
        }

        public void retry() throws ExecutionException, RetryException {
            build().call(() -> sender.trySend(failMessages));
        }

        /**
         * 每次重试时, 所需要发送的消息都能比上一次少(上一次发送成功了一部分)
         * 所以每次重试时, 都要进行分组过滤
         */
        @Override
        public <V> void onRetry(Attempt<V> attempt) {

            @SuppressWarnings("unchecked")
            List<MessageSendResult> retryResults = (List<MessageSendResult>) attempt.getResult();

            Set<String> successIds = retryResults.stream()
                    .filter(MessageSendResult::isSuccess)
                    .map(MessageSendResult::getMessageId)
                    .collect(Collectors.toSet());

            Map<Boolean, List<SendMessage>> retryResultsMap = failMessages.stream()
                    .collect(Collectors.partitioningBy(m -> successIds.contains(m.getMessageId())));

            retrySuccessMessages.addAll(retryResultsMap.get(true));

            failMessages = retryResultsMap.get(false);
            failMessages.forEach(m -> m.addRetries(1));
        }

        @Override
        public boolean apply(List<MessageSendResult> results) {
            return CollectionUtils.isNotEmpty(failMessages);
        }

        @Override
        public StopStrategy getStopStrategy() {
            return StopStrategies.stopAfterAttempt(RETRIES);
        }

        @Override
        public WaitStrategy getWaitStrategy() {
            // 1s 2s 4s 8s 16s 32s 60s 60s 60s ...
            return WaitStrategies.exponentialWait(500, 1, TimeUnit.MINUTES);
        }
    }

}
