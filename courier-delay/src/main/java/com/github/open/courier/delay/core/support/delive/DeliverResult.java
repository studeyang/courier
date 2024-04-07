package com.github.open.courier.delay.core.support.delive;

import com.github.open.courier.core.transport.SendMessage;
import com.github.open.courier.core.transport.MessageSendResult;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 延迟消息投递结果
 *
 * @author wangyonglin
 */
@Data
@AllArgsConstructor
public class DeliverResult {

    /**
     * 延迟消息投递结果
     */
    public List<MessageSendResult> sendResults;

    /**
     * 投递成功的消息
     */
    public List<SendMessage> successMessages;

    /**
     * 投递失败的消息
     */
    public List<SendMessage> failMessages;

}
