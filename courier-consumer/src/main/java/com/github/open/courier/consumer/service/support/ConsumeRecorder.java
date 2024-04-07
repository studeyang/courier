package com.github.open.courier.consumer.service.support;

import com.github.open.courier.core.transport.ConsumeMessage;
import com.github.open.courier.repository.mapper.ConsumeFailMessageMapper;
import com.github.open.courier.repository.mapper.ConsumeRecordMapper;
import com.github.open.courier.core.converter.ConsumeFailMessageConverter;
import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.core.transport.ConsumeState;
import com.github.open.courier.core.vo.UpdateConsumeRecord;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消费记录器, 记录消费成功和消费失败的场景
 */
@Component
public class ConsumeRecorder {

    @Autowired
    private ConsumeRecordMapper consumeRecordMapper;
    @Autowired
    private ConsumeFailMessageMapper consumeFailMessageMapper;

    /**
     * 消费成功, 更改Record表的state和时间字段
     */
    public void consumeSuccess(PushContext context, Date beforePushTime) {
        List<ConsumeMessage> messages = context.getMessages();

        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        List<String> successIds = new ArrayList<>(messages.size());
        Date startTime = new Date();
        Date endTime = new Date();
        for (ConsumeMessage message : messages) {
            successIds.add(message.getId());
            Date pollTime = message.getPollTime();
            if (startTime.getTime() > pollTime.getTime()) {
                startTime = pollTime;
            }
            if (endTime.getTime() < pollTime.getTime()) {
                endTime = pollTime;
            }
        }
        UpdateConsumeRecord update = new UpdateConsumeRecord()
                .setBeforePushTime(beforePushTime)
                .setEndPushTime(new Date())
                .setRetries(context.getRetries())
                .setIds(successIds)
                .setPollTimeBegin(startTime)
                .setPollTimeEnd(endTime);
        consumeRecordMapper.updateByIdsSelective(update);
    }

    /**
     * 消费失败, 新增fail表, 更新Record表的needRepush字段
     */
    public void consumeFails(PushContext context, String cause) {
        List<ConsumeMessage> messages = context.getMessages();

        List<ConsumeFailMessage> failMessages = ConsumeFailMessageConverter.toConsumeFailMessages(messages, cause);

        consumeFailMessageMapper.insertBatch(failMessages);

        // 让fail表通过management进行重试, record表设置need_repush=false, 不进行超时重试
        List<Date> dates = messages.stream().map(ConsumeMessage::getPollTime).collect(Collectors.toList());

        UpdateConsumeRecord update = new UpdateConsumeRecord()
                .setNeedRepush(false)
                .setState(ConsumeState.PUSH_FAIL)
                .setIds(failMessages.stream().map(ConsumeFailMessage::getId).collect(Collectors.toList()))
                .setPollTimeBegin(Collections.min(dates))
                .setPollTimeEnd(Collections.max(dates));

        consumeRecordMapper.updateByIdsSelective(update);
    }
}
