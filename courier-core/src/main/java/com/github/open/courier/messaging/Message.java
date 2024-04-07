package com.github.open.courier.messaging;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;

import java.util.Date;

import com.github.open.courier.core.constant.MessageConstant;
import com.github.open.courier.core.message.Usage;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消息
 */
@Data
@Accessors(chain = true)
@JsonTypeInfo(use = CLASS, property = MessageConstant.MESSAGE_TYPE)
public abstract class Message {

    /**
     * 消息id
     */
    private String id;

    /**
     * 主键
     */
    private String primaryKey;

    /**
     * 用途
     */
    private Usage usage;

    /**
     * 所在服务
     */
    private String service;

    /**
     * 主题
     * topic依然需要赋值, APP要用到json的这个字段, 坑爹...
     */
    private String topic;

    /**
     * 创建时间
     * timeStamp依然需要赋值, APP要用到json的这个字段, 坑爹...
     */
    private Date timeStamp;

    private String clientVersion;
}
