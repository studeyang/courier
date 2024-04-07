package com.github.open.courier.core.transport;

import java.util.Date;

import com.github.open.courier.core.message.Usage;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 消费消息
 */
@Data
@Accessors(chain = true)
public class ConsumeMessage {

    /**
     * 消费ID
     */
    private String id;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 消息的topic
     */
    private String topic;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 消费组id
     */
    private String groupId;

    /**
     * 消息发送方的service
     */
    private String fromService;

    /**
     * 消息消费方的service
     */
    private String toService;

    /**
     * 消息体
     */
    private String content;

    /**
     * 消息的创建时间
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
     * 消息的重试发送次数
     */
    private Integer retries;

    /**
     * 从客户端是否需要失败重试
     */
    private Boolean needRepush;

    /**
     * 消息从kafka拉取时间
     */
    private Date pollTime;

    /**
     * 集群名称
     */
    private String fromCluster;

    /**
     * 环境名称
     */
    private String fromEnv;
}
