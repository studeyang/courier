package com.github.open.courier.core.transport;

import com.github.open.courier.core.message.Usage;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用于存储的全量消息
 */
@Data
@Accessors(chain = true)
public class DBMessage {

    /**
     * 消息id
     */
    private String messageId;

    /**
     * 主题
     */
    private String topic;

    /**
     * 消息类型全写
     */
    private String type;

    /**
     * 消息类型简写
     */
    private String event;

    /**
     * 消息发送方的cluster
     */
    private String cluster;

    /**
     * 消息发送方的env
     */
    private String env;

    /**
     * 消息发送方的service
     */
    private String fromService;

    /**
     * 消息体
     */
    private String content;

    /**
     * 消息创建时间
     */
    private Date createdAt;

    /**
     * 消息主键
     */
    private String primaryKey;

    /**
     * 用途
     */
    private Usage usage;

    /**
     * kafka偏移量
     */
    private long offset;

    /**
     * kafka分区
     */
    private int partition;
}