package com.github.open.courier.client.producer.sender;

import com.github.open.courier.core.transport.SendMessage;

import java.util.List;

/**
 * 消息报告
 *
 * @author yanglulu
 */
public interface Reportable {

    /**
     * 在发送成功后的处理操作
     *
     * @param successMessage 发送成功的消息
     */
    void onSuccess(SendMessage successMessage);

    /**
     * 在发送成功后的处理操作
     *
     * @param successMessages 发送成功的一批消息
     */
    void onSuccess(List<SendMessage> successMessages);

    /**
     * 在重试失败后的处理操作
     *
     * @param failMessage 发送失败的消息
     */
    void onFail(SendMessage failMessage);

    /**
     * 在重试失败后的处理操作
     *
     * @param failMessages 发送失败的一批消息
     */
    void onFail(List<SendMessage> failMessages);

    /**
     * 在重试失败后, 报告给management成功后的处理操作
     *
     * @param retryFailMessage 重试失败的消息
     */
    void onReport(SendMessage retryFailMessage);

    /**
     * 在重试失败后, 报告给management成功后的处理操作
     *
     * @param retryFailMessages 重试失败的一批消息
     */
    void onReport(List<SendMessage> retryFailMessages);

}
