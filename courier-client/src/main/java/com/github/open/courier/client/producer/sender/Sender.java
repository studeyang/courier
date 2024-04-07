package com.github.open.courier.client.producer.sender;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.MessageSendResult;

import java.util.List;

/**
 * 请求发送器
 *
 * @author yanglulu
 */
public interface Sender {

    /**
     * 尝试发送，可能会失败
     *
     * @param message 消息
     * @return 发送结果
     */
    MessageSendResult trySend(SendMessage message);

    /**
     * 尝试发送，可能会失败
     *
     * @param messages 一批消息
     * @return 发送结果
     */
    List<MessageSendResult> trySend(List<SendMessage> messages);

}
