package com.github.open.courier.core.transport;

import lombok.Data;

/**
 * 2 * @Author: chengyan
 * 3 * @Date: 2020/12/3 15:45
 */
@Data
public class DelayMessageRedisEntity {

    private String messageId;

    private Long endTime;


    public static DelayMessageRedisEntity create(String messageId, Long endTime){
        DelayMessageRedisEntity entity = new DelayMessageRedisEntity();
        entity.setMessageId(messageId);
        entity.setEndTime(endTime);
        return entity;
    }
}
