package com.github.open.courier.delay.biz;

import com.github.open.courier.core.transport.DelayMessage;
import com.github.open.courier.core.transport.MessageSendResult;
import com.github.open.courier.delay.core.ScheduleManager;
import com.github.open.courier.repository.mapper.DelayMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yanglulu
 */
@Slf4j
@Service
public class DelayMessageBizService {

    @Autowired
    private DelayMessageMapper delayMessageMapper;

    @Autowired
    private ScheduleManager scheduleManager;


    public boolean insert(DelayMessage delayMessage) {

        try {
            delayMessageMapper.insert(delayMessage);
            return true;
        } catch (Exception e) {
            log.error("存入延迟消息暂存表失败：message：{}, 原因：{}", delayMessage, e);
            return false;
        }
    }


    public boolean insertList(List<DelayMessage> delayMessages) {

        try {
            delayMessageMapper.insertList(delayMessages);
            return true;
        } catch (Exception e) {
            List<String> messageIds = delayMessages.stream().map(DelayMessage::getMessageId).collect(Collectors.toList());
            log.error("批量存入延迟消息暂存表失败, messageIds：{}, 原因：{}", messageIds, e);
            return false;
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public MessageSendResult handleInsidePreReadRange(DelayMessage delayMessage) {

        if (!insert(delayMessage)) {
            return MessageSendResult.error(delayMessage.getMessageId(), "延迟消息存入数据库失败");
        }

        if (!scheduleManager.pushRedisTimeWheel(delayMessage)) {

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return MessageSendResult.error(delayMessage.getMessageId(), "延迟消息推入时间轮失败");
        }

        return MessageSendResult.success(delayMessage.getMessageId());

    }


    @Transactional(rollbackFor = Exception.class)
    public List<MessageSendResult> handleInsidePreReadRange(List<DelayMessage> delayMessages, long expireTime) {

        if (!insertList(delayMessages)) {
            return toFail(delayMessages, "延迟消息批量存入数据库失败");
        }

        if (!scheduleManager.pushRedisTimeWheel(delayMessages, expireTime)) {

            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            return toFail(delayMessages, "延迟消息批量推入时间轮失败");
        }

        return toSuccess(delayMessages);
    }


    public List<MessageSendResult> toSuccess(List<DelayMessage> delayMessages) {

        return delayMessages.stream()
                .map(o -> MessageSendResult.success(o.getMessageId())).collect(Collectors.toList());
    }


    public List<MessageSendResult> toFail(List<DelayMessage> delayMessages, String failReason) {

        return delayMessages.stream()
                .map(o -> MessageSendResult.error(o.getMessageId(), failReason)).collect(Collectors.toList());
    }

}
