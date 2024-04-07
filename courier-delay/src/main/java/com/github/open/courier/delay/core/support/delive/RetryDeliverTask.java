package com.github.open.courier.delay.core.support.delive;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.support.Retryable;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.rholder.retry.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 延迟消息重试投递任务
 *
 * @author wangyonglin
 */
@Slf4j
public class RetryDeliverTask extends DeliverTask {



    public RetryDeliverTask(DeliverSupport deliverSupport,
                            List<String> messageIds,
                            long forwardSearchTime,
                            long backwardSearchTime,
                            long wheelStartTime,
                            int skipPoint,
                            long skipTime) {

        super(deliverSupport,
                messageIds,
                forwardSearchTime,
                backwardSearchTime,
                wheelStartTime,
                skipPoint,
                skipTime);

    }


    @SneakyThrows
    @Override
    public void run() {

        long monitorStartTime = System.currentTimeMillis();

        log.info("重试投递任务信息：[wheelStartTime:{}, skipPoint:{}, skipTime:{}, size:{}, forwardSearchTime:{}, backwardSearchTime:{}, messageIds:{}]",
                wheelStartTime, skipPoint, skipTime, messageIds.size(), forwardSearchTime, backwardSearchTime, messageIds);

        Date startDeliveTime = new Date();

        MultipleRetryerContext context = new MultipleRetryerContext(deliverSupport, messageIds, forwardSearchTime, backwardSearchTime);

        try {
            context.retry();
        } catch (RetryException e) {
            log.info("消息重试投递任务失败，messageIds：{}", messageIds);
        }

        Date endDeliveTime = new Date();

        deliverSupport.handleSuccess(context.getRetrySuccessMessages(), forwardSearchTime, backwardSearchTime, startDeliveTime, endDeliveTime);

        deliverSupport.reportSendFail(context.getRetryFailMessages(), context.getRetryResults());

        long monitorEndTime = System.currentTimeMillis();

        log.info("执行消息重试投递任务, 操作开始时间：{}, 操作结束时间：{}, 投递耗时：{}", monitorStartTime, monitorEndTime, monitorEndTime - monitorStartTime);
    }



    @Getter
    static class MultipleRetryerContext implements Retryable<DeliverResult> {

        private final DeliverSupport deliverSupport;
        /**
         * 待重试的消息
         */
        private List<String> failMessageIds;
        private final long forwardSearchTime;
        private final long backwardSearchTime;
        private final List<SendMessage> retryFailMessages;
        private final List<SendMessage> retrySuccessMessages;
        private final List<MessageSendResult> retryResults;


        public MultipleRetryerContext(DeliverSupport deliverSupport, List<String> failMessageIds,
                                      long forwardSearchTime, long backwardSearchTime) {
            this.deliverSupport = deliverSupport;
            this.failMessageIds = failMessageIds;
            this.forwardSearchTime = forwardSearchTime;
            this.backwardSearchTime = backwardSearchTime;
            this.retryFailMessages = Lists.newArrayListWithCapacity(failMessageIds.size());
            this.retrySuccessMessages = Lists.newArrayListWithCapacity(failMessageIds.size());
            this.retryResults = Lists.newArrayListWithCapacity(failMessageIds.size());
        }

        public void retry() throws ExecutionException, RetryException {
            build().call(() -> deliverSupport.trySend(failMessageIds, forwardSearchTime, backwardSearchTime));
        }

        /**
         * 每次重试时, 所需要发送的消息都能比上一次少(上一次发送成功了一部分)
         * 所以每次重试时, 都要进行分组过滤
         */
        @Override
        public <V> void onRetry(Attempt<V> attempt) {

            @SuppressWarnings("unchecked")
            DeliverResult deliverResult = (DeliverResult) attempt.getResult();

            List<MessageSendResult> sendResults = deliverResult.getSendResults();
            List<SendMessage> successMessages = deliverResult.getSuccessMessages();
            List<SendMessage> failMessages = deliverResult.getFailMessages();

            List<String> allMessageIds = sendResults.stream().map(MessageSendResult::getMessageId).collect(Collectors.toList());
            retrySuccessMessages.addAll(successMessages);
            failMessageIds = failMessages.stream().map(SendMessage::getMessageId).collect(Collectors.toList());

            // 每次重试保存结果时，需要把上一次失败的结果都移除，重新放入
            sendResults.removeIf(result -> allMessageIds.contains(result.getMessageId()));

            retryResults.addAll(sendResults);
        }

        @Override
        public boolean apply(DeliverResult result) {
            return CollectionUtils.isNotEmpty(failMessageIds);
        }

        @Override
        public StopStrategy getStopStrategy() {
            return StopStrategies.stopAfterAttempt(3);
        }

        @Override
        public WaitStrategy getWaitStrategy() {
            // 100ms 100ms 100ms ...
            return WaitStrategies.fixedWait(100, TimeUnit.MILLISECONDS);
        }
    }


}
