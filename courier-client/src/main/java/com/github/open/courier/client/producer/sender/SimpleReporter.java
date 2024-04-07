package com.github.open.courier.client.producer.sender;

import com.github.open.courier.core.transport.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @author yanglulu
 */
@Slf4j
public class SimpleReporter implements Reportable {

    @Override
    public void onSuccess(SendMessage successMessage) {
        if (successMessage != null) {
            log.info("kafka发送消息成功, message:{}", successMessage.getContent());
        }
    }

    @Override
    public void onSuccess(List<SendMessage> successMessages) {
        if (CollectionUtils.isNotEmpty(successMessages)) {
            for (int i = 0; i < successMessages.size(); i++) {
                log.info("kafka发送批量消息成功, message-{}: {}", i + 1, successMessages.get(i).getContent());
            }
        }
    }

    @Override
    public void onFail(SendMessage failMessage) {
        if (failMessage != null) {
            log.error("kafka发送消息失败, message:{}", failMessage.getContent());
        }
    }

    @Override
    public void onFail(List<SendMessage> failMessages) {
        if (CollectionUtils.isNotEmpty(failMessages)) {
            for (int i = 0; i < failMessages.size(); i++) {
                log.error("kafka发送批量消息失败, message-{}: {}", i + 1, failMessages.get(i).getContent());
            }
        }
    }

    @Override
    public void onReport(SendMessage retryFailMessage) {
        // do nothing
    }

    @Override
    public void onReport(List<SendMessage> retryFailMessages) {
        // do nothing
    }
}
