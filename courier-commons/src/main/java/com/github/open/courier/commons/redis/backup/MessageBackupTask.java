package com.github.open.courier.commons.redis.backup;

import com.github.open.courier.commons.redis.RedisHelper;
import com.github.open.courier.core.converter.MessageJsonConverter;
import com.github.open.courier.repository.mapper.MessageMapper;
import com.github.open.courier.core.transport.DBMessage;
import com.github.open.courier.core.transport.MessageQueryCondition;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.github.open.courier.core.constant.MessageConstant.BACKUP_MESSAGES_TIMEOUT;
import static com.github.open.courier.core.constant.MessageConstant.KAFKA_BACKUP_MESSAGES;

/**
 * @author Courier
 */
@Slf4j
@RequiredArgsConstructor
public class MessageBackupTask implements Runnable {

    private static final String EMPTY_VALUE = "[]";

    private final MessageMapper messageMapper;
    private final RedisHelper redisHelper;

    @Getter
    private volatile boolean taskSwitch = true;

    @Override
    public void run() {
        backup();
    }

    /**
     * 考虑到消息的时效性，消息失败之后可能会直接重试，
     * 拉到消息之后立刻入库
     */
    private void backup() {

        while (taskSwitch) {

            Object value = "";

            try {
                value = redisHelper.rightPop(KAFKA_BACKUP_MESSAGES, BACKUP_MESSAGES_TIMEOUT, TimeUnit.SECONDS);

                if (isEmpty(value)) {
                    continue;
                }
                List<DBMessage> messages = MessageJsonConverter.toObject(value.toString(), new TypeReference<List<DBMessage>>() {
                });

                if (CollectionUtils.isEmpty(messages)) {
                    continue;
                }

                long start = System.currentTimeMillis();
                insertIfNotExist(messages);

                if (log.isInfoEnabled()) {
                    long end = System.currentTimeMillis();
                    long delay = Optional.ofNullable(messages.get(0).getCreatedAt())
                            .map(date -> end - date.getTime())
                            .orElse(-1L);
                    long cost = end - start;
                    log.info("backup size={}, cost={}, delay={}", messages.size(), cost, delay);
                }
            } catch (Exception e) {
                log.error("消息入库失败, message: {}", value.toString(), e);
            }
        }
    }

    private void insertIfNotExist(List<DBMessage> messages) {

        MessageQueryCondition queryCondition = new MessageQueryCondition();
        queryCondition.setMessageIds(messages.stream().map(DBMessage::getMessageId).collect(Collectors.toList()));

        List<Date> dates = messages.stream().map(DBMessage::getCreatedAt).collect(Collectors.toList());

        queryCondition.setStartTime(Collections.min(dates));
        queryCondition.setEndTime(Collections.max(dates));

        // 查询出已经存在的

        Set<String> existedMessageIds = messageMapper.listByMessageIds(queryCondition)
                .stream()
                .map(DBMessage::getMessageId)
                .collect(Collectors.toSet());

        List<DBMessage> toInsertList = messages.stream()
                .filter(message -> !existedMessageIds.contains(message.getMessageId()))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(toInsertList)) {
            messageMapper.insertBatch(toInsertList);
        }
    }

    /**
     * json 工具传入空数据的时候回返回 "[]"
     */
    private boolean isEmpty(Object value) {

        if (value == null) {
            return true;
        }

        if (EMPTY_VALUE.equals(value)) {
            log.warn("value isEmpty, value:{}", value);
            return true;
        }

        return false;
    }

    public void stop() {

        taskSwitch = false;
    }
}
