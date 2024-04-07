package com.github.open.courier.core.converter;

import com.github.open.courier.core.utils.DateUtils;
import com.github.open.courier.core.transport.ConsumeFailMessage;
import com.github.open.courier.core.transport.ConsumeRecord;
import com.github.open.courier.core.transport.MessageQueryCondition;
import com.github.open.courier.core.transport.QueryOperationRequest;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 2 * @Author: chengyan
 * 3 * @Date: 2020/9/17 17:26
 */
@Slf4j
public class MessageQueryConditionConverter {

    private MessageQueryConditionConverter() {
    }

    public static MessageQueryCondition toMessageQueryConditionByRecord(List<ConsumeRecord> records) {
        MessageQueryCondition result = new MessageQueryCondition();
        List<String> messageIds = Lists.newArrayList();
        for (ConsumeRecord record : records) {
            messageIds.add(record.getMessageId());
            // message_record.pollTime 肯定是比 message.createAt 大，会导致查不到 message 的情况
            // todo 先查当天的，如果后面出现这种情况多的话，再改为广播模式。
            Date startTime = result.getStartTime();
            Date endTime = result.getEndTime();
            Date pollTime = record.getPollTime();
            if (startTime == null && endTime == null) {
                result.setStartTime(pollTime);
                result.setEndTime(pollTime);
            }

            if (startTime != null && startTime.getTime() > pollTime.getTime()) {
                result.setStartTime(pollTime);
            } else if (endTime != null && endTime.getTime() < pollTime.getTime()) {
                result.setEndTime(pollTime);
            }
        }
        result.setMessageIds(messageIds);
        updateDate(result);
        return result;
    }

    /**
     * 再消费时，消息记录表的 poll_time 必定小于消息表的 createAt
     *
     * @param result
     */
    private static void updateDate(MessageQueryCondition result) {

        SimpleDateFormat timeDay = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeSS = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        // todo 如果出现消息积压的话积压时间超过一天，这里还是会有问题，如果走广播的会严重影响性能，先查当天的，极端情况手动处理，消息积压也是需要解决的
        result.setStartTime(getStartDate(timeDay, timeSS, result.getStartTime(), " 00:00:00"));
        result.setEndTime(getStartDate(timeDay, timeSS, result.getEndTime(), " 23:59:59"));
    }

    private static Date getStartDate(SimpleDateFormat timeDay, SimpleDateFormat timeSS,
                                     Date date, String suffix) {

        String temp = timeDay.format(date);
        try {
            return timeSS.parse(temp + suffix);
        } catch (Exception e) {
            return null;
        }
    }

    public static MessageQueryCondition toMessageQueryConditionByFail(List<ConsumeFailMessage> fails) {

        MessageQueryCondition result = new MessageQueryCondition();

        List<String> messageIds = Lists.newArrayList();
        List<Date> dates = new ArrayList<>(fails.size());
        for (ConsumeFailMessage fail : fails) {
            messageIds.add(fail.getMessageId());
            dates.add(fail.getCreatedAt());
        }

        Date startTime = DateUtils.parseDate(Collections.min(dates), "00:00:00");
        Date endTime = DateUtils.parseDate(Collections.max(dates), "23:59:59");

        result.setStartTime(startTime);
        result.setEndTime(endTime);
        result.setMessageIds(messageIds);
        return result;
    }

    public static MessageQueryCondition toMessageQueryCondition(QueryOperationRequest operationRequest) {
        MessageQueryCondition result = new MessageQueryCondition();
        try {
            result.setMessageIds(operationRequest.getIds());
            result.setStartTime(DateUtils.parse(operationRequest.getStartTime()));
            result.setEndTime(DateUtils.parse(operationRequest.getEndTime()));
        } catch (ParseException e) {
            String cause = ExceptionConverter.getCause(e);
            log.error("时间转换出现异常, message:{}", cause);
            // 异常时，扫描前两天的
            result.setStartTime(DateUtils.getDateBefore(2));
            result.setEndTime(new Date());
        }
        return result;
    }


}
